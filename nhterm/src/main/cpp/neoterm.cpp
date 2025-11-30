#include <dirent.h>
#include <fcntl.h>
#include <jni.h>
#include <csignal>
#include <cstdio>
#include <cstdlib>
#include <sys/ioctl.h>
#include <sys/wait.h>
#include <termios.h>
#include <unistd.h>
#include <cstring>
#include <cerrno>
#include <climits>

#define TERMUX_UNUSED(x) x __attribute__((__unused__))
#ifdef __APPLE__
# define LACKS_PTSNAME_R
#endif

static int throw_runtime_exception(JNIEnv *env, char const *message) {
    jclass exClass = env->FindClass("java/lang/RuntimeException");
    env->ThrowNew(exClass, message);
    return -1;
}

static int create_subprocess(JNIEnv *env,
                             char const *cmd,
                             char const *cwd,
                             char *const argv[],
                             char **envp,
                             int *pProcessId,
                             jint rows,
                             jint columns) {
    int ptm = open("/dev/ptmx", O_RDWR | O_CLOEXEC);
    if (ptm < 0) return throw_runtime_exception(env, "Cannot open /dev/ptmx");

#ifdef LACKS_PTSNAME_R
    char* devname;
#else
    char devname[64];
#endif
    if (grantpt(ptm) || unlockpt(ptm) ||
        #ifdef LACKS_PTSNAME_R
        (devname = ptsname(ptm)) == nullptr
        #else
        ptsname_r(ptm, devname, sizeof(devname))
#endif
            ) {
        close(ptm);
        return throw_runtime_exception(env, "Cannot grantpt()/unlockpt()/ptsname_r() on /dev/ptmx");
    }

    // Enable UTF-8 mode and disable flow control to prevent Ctrl+S from locking up the display.
    struct termios tios = {};
    tcgetattr(ptm, &tios);
    tios.c_iflag |= IUTF8;
    tios.c_iflag &= ~(IXON | IXOFF);
    tcsetattr(ptm, TCSANOW, &tios);

    /** Set initial winsize. */
    struct winsize sz = {.ws_row = static_cast<unsigned short>(rows), .ws_col = static_cast<unsigned short>(columns)};
    ioctl(ptm, TIOCSWINSZ, &sz);

    pid_t pid = fork();
    if (pid < 0) {
        close(ptm);
        return throw_runtime_exception(env, "Fork failed");
    } else if (pid > 0) {
        *pProcessId = (int) pid;
        return ptm;
    } else {
        // Clear signals which the Android java process may have blocked:
        sigset_t signals_to_unblock;
        sigfillset(&signals_to_unblock);
        sigprocmask(SIG_UNBLOCK, &signals_to_unblock, nullptr);

        close(ptm);
        setsid();

        int pts = open(devname, O_RDWR);
        if (pts < 0) exit(-1);

        dup2(pts, 0);
        dup2(pts, 1);
        dup2(pts, 2);

        DIR *self_dir = opendir("/proc/self/fd");
        if (self_dir != nullptr) {
            int self_dir_fd = dirfd(self_dir);
            struct dirent *entry;
            while ((entry = readdir(self_dir)) != nullptr) {
                /*
                 * Use strtol instead of atoi to detect conversion errors and range
                 * issues. Skip directory entries that are not purely numeric
                 * (e.g. "." and ".."). Also protect against values outside the
                 * range of int and negative values.
                 */
                errno = 0;
                char *endptr = nullptr;
                long fd_long = strtol(entry->d_name, &endptr, 10);

                /* Not a number or contains non-digit suffix */
                if (endptr == entry->d_name || *endptr != '\0') continue;

                /* Conversion overflow/underflow */
                if (errno == ERANGE) continue;

                /* Out-of-range for int, negative, or standard fds we must keep */
                if (fd_long <= 2 || fd_long == self_dir_fd || fd_long > INT_MAX) continue;

                int fd = static_cast<int>(fd_long);
                close(fd);
            }
            closedir(self_dir);
        }

        clearenv();
        if (envp) for (; *envp; ++envp) putenv(*envp);

        if (chdir(cwd) != 0) {
            char *error_message;
            // No need to free asprintf()-allocated memory since doing execvp() or exit() below.
            if (asprintf(&error_message, "chdir(\"%s\")", cwd) == -1)
                error_message = const_cast<char *>("chdir()");
            perror(error_message);
            fflush(stderr);
        }
        execvp(cmd, argv);
        // Show terminal output about failing exec() call:
        char *error_message;
        if (asprintf(&error_message, "exec(\"%s\")", cmd) == -1)
            error_message = const_cast<char *>("exec()");
        perror(error_message);
         _exit(1);
    }
}

extern "C" JNIEXPORT jint JNICALL Java_com_offsec_nhterm_backend_JNI_createSubprocess(
         JNIEnv *env,
         jclass TERMUX_UNUSED(clazz),
         jstring cmd,
         jstring cwd,
         jobjectArray args,
         jobjectArray envVars,
         jintArray processIdArray,
         jint rows,
         jint columns) {
    char **argv = nullptr;
    char **envp = nullptr;
    char const *cmd_cwd = nullptr;
    char const *cmd_utf8 = nullptr;
    int ptm = -1;

    // Lambda for cleanup to prevent memory leaks on error paths
    auto cleanup = [&]() {
        if (argv) {
            for (char **tmp = argv; *tmp; ++tmp) free(*tmp);
            free(argv);
        }
        if (envp) {
            for (char **tmp = envp; *tmp; ++tmp) free(*tmp);
            free(envp);
        }
        if (cmd_utf8) env->ReleaseStringUTFChars(cmd, cmd_utf8);
        if (cmd_cwd) env->ReleaseStringUTFChars(cwd, cmd_cwd);
        if (ptm >= 0) close(ptm);
    };

    jsize size = args ? env->GetArrayLength(args) : 0;
    if (size > 0) {
        argv = (char **) malloc((size + 1) * sizeof(char *));
        if (!argv) {
            cleanup();
            return throw_runtime_exception(env, "Couldn't allocate argv array");
        }
        for (int i = 0; i < size; ++i) {
            jobject arg_obj = env->GetObjectArrayElement(args, i);
            auto arg_java_string = reinterpret_cast<jstring>(arg_obj);
            char const *arg_utf8 = env->GetStringUTFChars(arg_java_string, nullptr);
            if (!arg_utf8) {
                argv[i] = nullptr; // Mark end for cleanup
                cleanup();
                return throw_runtime_exception(env, "GetStringUTFChars() failed for argv");
            }
            argv[i] = strdup(arg_utf8);
            env->ReleaseStringUTFChars(arg_java_string, arg_utf8);
            if (!argv[i]) {
                argv[i + 1] = nullptr; // Mark end for cleanup
                cleanup();
                return throw_runtime_exception(env, "strdup() failed for argv");
            }
        }
        argv[size] = nullptr;
    }

    size = envVars ? env->GetArrayLength(envVars) : 0;
    if (size > 0) {
        envp = (char **) malloc((size + 1) * sizeof(char *));
        if (!envp) {
            cleanup();
            return throw_runtime_exception(env, "malloc() for envp array failed");
        }
        for (int i = 0; i < size; ++i) {
            jobject env_obj = env->GetObjectArrayElement(envVars, i);
            auto env_java_string = reinterpret_cast<jstring>(env_obj);
            char const *env_utf8 = env->GetStringUTFChars(env_java_string, nullptr);
            if (!env_utf8) {
                envp[i] = nullptr; // Mark end for cleanup
                cleanup();
                return throw_runtime_exception(env, "GetStringUTFChars() failed for env");
            }
            envp[i] = strdup(env_utf8);
            env->ReleaseStringUTFChars(env_java_string, env_utf8);
            if (!envp[i]) {
                envp[i + 1] = nullptr;
                cleanup();
                return throw_runtime_exception(env, "strdup() failed for envp");
            }
        }
        envp[size] = nullptr;
    }

    cmd_cwd = env->GetStringUTFChars(cwd, nullptr);
    if (!cmd_cwd) {
        cleanup();
        return throw_runtime_exception(env, "GetStringUTFChars() failed for cwd");
    }

    cmd_utf8 = env->GetStringUTFChars(cmd, nullptr);
    if (!cmd_utf8) {
        cleanup();
        return throw_runtime_exception(env, "GetStringUTFChars() failed for cmd");
    }

    int procId = 0;
    ptm = create_subprocess(env, cmd_utf8, cmd_cwd, argv, envp, &procId, rows, columns);

    // Release string references
    env->ReleaseStringUTFChars(cmd, cmd_utf8);
    cmd_utf8 = nullptr;
    env->ReleaseStringUTFChars(cwd, cmd_cwd);
    cmd_cwd = nullptr;

    // Free argv and envp arrays
    if (argv) {
        for (char **tmp = argv; *tmp; ++tmp) free(*tmp);
        free(argv);
        argv = nullptr;
    }
    if (envp) {
        for (char **tmp = envp; *tmp; ++tmp) free(*tmp);
        free(envp);
        envp = nullptr;
    }

    // Check if subprocess creation failed
    if (ptm < 0) {
        // Exception already set by create_subprocess
        return -1;
    }

    int *pProcId = static_cast<int *>(env->GetPrimitiveArrayCritical(processIdArray, nullptr));
    if (!pProcId) {
        close(ptm);
        return throw_runtime_exception(env,
                                       "JNI call GetPrimitiveArrayCritical(processIdArray, &isCopy) failed");
    }

    *pProcId = procId;
    env->ReleasePrimitiveArrayCritical(processIdArray, pProcId, 0);

    return ptm;
}

extern "C" JNIEXPORT void JNICALL
Java_com_offsec_nhterm_backend_JNI_setPtyWindowSize(JNIEnv *TERMUX_UNUSED(env),
                                               jclass TERMUX_UNUSED(clazz),
                                               jint fd, jint rows,
                                               jint cols) {
     struct winsize sz = {.ws_row = static_cast<unsigned short>(rows), .ws_col = static_cast<unsigned short>(cols)};
     ioctl(fd, TIOCSWINSZ, &sz);
 }

 extern "C" JNIEXPORT void JNICALL __attribute__((used))
 Java_com_offsec_nhterm_backend_JNI_setPtyUTF8Mode(JNIEnv *TERMUX_UNUSED(env), jclass TERMUX_UNUSED(clazz),
                                             jint fd) {
    struct termios tios = {};
    tcgetattr(fd, &tios);
    if ((tios.c_iflag & IUTF8) == 0) {
        tios.c_iflag |= IUTF8;
        tcsetattr(fd, TCSANOW, &tios);
    }
 }

 extern "C" JNIEXPORT int JNICALL
 Java_com_offsec_nhterm_backend_JNI_waitFor(JNIEnv *TERMUX_UNUSED(env), jclass TERMUX_UNUSED(clazz),
                                      jint pid) {
     int status;
     waitpid(pid, &status, 0);
     if (WIFEXITED(status)) {
         return WEXITSTATUS(status);
     } else if (WIFSIGNALED(status)) {
         return -WTERMSIG(status);
     } else {
         // Should never happen - waitpid(2) says "One of the first three macros will evaluate to a non-zero (true) value".
         return 0;
     }
 }

 extern "C" JNIEXPORT void JNICALL
 Java_com_offsec_nhterm_backend_JNI_close(JNIEnv *TERMUX_UNUSED(env), jclass TERMUX_UNUSED(clazz),
                                    jint fileDescriptor) {
     close(fileDescriptor);
 }
