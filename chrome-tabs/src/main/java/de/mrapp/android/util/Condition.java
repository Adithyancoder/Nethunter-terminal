package de.mrapp.android.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * Minimal replacement for the external Condition utility used by the project.
 * Provides a few static precondition checks used across the module.
 */
public final class Condition {

  private Condition() {
    // Utility class
  }

  private static RuntimeException createException(@NonNull final Class<? extends RuntimeException> clazz,
                                                   @NonNull final String message) {
    try {
      Constructor<? extends RuntimeException> ctor = clazz.getConstructor(String.class);
      return ctor.newInstance(message);
    } catch (Exception e) {
      return new RuntimeException(message, e);
    }
  }

  public static void ensureNotNull(@Nullable final Object object, @NonNull final String message) {
    if (object == null) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void ensureNotNull(@Nullable final Object object, @NonNull final String message,
                                   @NonNull final Class<? extends RuntimeException> exceptionClass) {
    if (object == null) {
      throw createException(exceptionClass, message);
    }
  }

  public static void ensureNotEmpty(@Nullable final CharSequence sequence, @NonNull final String message) {
    if (sequence == null || sequence.length() == 0) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void ensureAtLeast(final long value, final long min, @NonNull final String message) {
    if (value < min) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void ensureNotEqual(@Nullable final Object a, @Nullable final Object b, @NonNull final String message) {
    if (a == b) {
      throw new IllegalArgumentException(message);
    }
    if (a != null && a.equals(b)) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void ensureNotEqual(@Nullable final Object a, @Nullable final Object b, @NonNull final String message,
                                    @NonNull final Class<? extends RuntimeException> exceptionClass) {
    if (Objects.equals(a, b)) {
      throw createException(exceptionClass, message);
    }
  }

  public static void ensureTrue(final boolean condition, @NonNull final String message) {
    if (!condition) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void ensureTrue(final boolean condition, @NonNull final String message,
                                @NonNull final Class<? extends RuntimeException> exceptionClass) {
    if (!condition) {
      throw createException(exceptionClass, message);
    }
  }

  public static void ensureEqual(final int a, final int b, @NonNull final String message) {
    if (a != b) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void ensureGreater(final int value, final int minExclusive, @NonNull final String message) {
    if (value <= minExclusive) {
      throw new IllegalArgumentException(message);
    }
  }

}
