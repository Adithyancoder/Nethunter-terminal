package com.offsec.nhterm.setup

import android.content.Context
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.offsec.nhterm.App
import com.offsec.nhterm.R
import com.offsec.nhterm.component.config.NeoTermPath
import java.io.File

/**
 * @author kiva
 */
interface ResultListener {
  fun onResult(error: Exception?)
}

object SetupHelper {
  fun needSetup(): Boolean {
    val PREFIX_FILE = File(NeoTermPath.USR_PATH)
    return !PREFIX_FILE.isDirectory
  }

  fun setup(
      activity: AppCompatActivity, connection: SourceConnection,
      resultListener: ResultListener
  ) {
    if (!needSetup()) {
      resultListener.onResult(null)
      return
    }

    val prefixFile = File(NeoTermPath.USR_PATH)
    val progress = makeProgressDialog(activity)

    progress.show()

    SetupThread(
        activity,
        connection,
        prefixFile,
        resultListener,
        progress
    ).start()
  }

  private fun makeProgressDialog(context: Context): AlertDialog {
    return makeProgressDialog(context, context.getString(R.string.installer_message))
  }

  fun makeProgressDialog(context: Context, message: String): AlertDialog {
    // Build a simple vertical layout: message TextView + horizontal ProgressBar
    val container = LinearLayout(context)
    container.orientation = LinearLayout.VERTICAL
    val lp = LinearLayout.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.WRAP_CONTENT
    )
    val msgView = TextView(context)
    msgView.text = message
    msgView.setPadding(32, 24, 32, 8)
    container.addView(msgView, lp)

    val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
    progressBar.isIndeterminate = false
    progressBar.max = 100

    progressBar.id = android.R.id.progress
    val barLp = FrameLayout.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.WRAP_CONTENT
    )
    barLp.setMargins(32, 8, 32, 24)
    val frame = FrameLayout(context)
    frame.addView(progressBar, barLp)
    container.addView(frame, lp)

    val dialog = MaterialAlertDialogBuilder(context, R.style.DialogStyle)
      .setView(container)
      .setCancelable(false)
      .create()

    return dialog
  }

  fun makeErrorDialog(context: Context, messageId: Int): AlertDialog {
    return makeErrorDialog(context, context.getString(messageId))
  }

  fun makeErrorDialog(context: Context, message: String): AlertDialog {
    return MaterialAlertDialogBuilder(context, R.style.DialogStyle)
      .setTitle(R.string.error)
      .setMessage(message)
      .setPositiveButton(android.R.string.ok, null)
      .setNeutralButton(R.string.show_help) { _, _ -> App.get().openHelpLink() }
      .create()
  }

  fun determineArchName(): String {
    for (androidArch in Build.SUPPORTED_ABIS) {
      when (androidArch) {
        "arm64-v8a" -> return "aarch64"
        "armeabi-v7a" -> return "arm"
        "x86_64" -> return "x86_64"
      }
    }
    throw RuntimeException(
      "Unable to determine arch from Build.SUPPORTED_ABIS =  "
        + Build.SUPPORTED_ABIS.contentToString()
    )
  }
}
