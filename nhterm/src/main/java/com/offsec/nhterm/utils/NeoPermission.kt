package com.offsec.nhterm.utils

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.content.pm.PackageManager
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.offsec.nhterm.R

/**
 * Permission helper that centralizes app permission logic.
 */
object NeoPermission {
  const val REQUEST_APP_PERMISSION = 10086

  fun initAppPermission(context: AppCompatActivity, requestCode: Int) {
    // For Android 11+ apps that need broad file access must use MANAGE_EXTERNAL_STORAGE
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      if (!Environment.isExternalStorageManager()) {
        // Explain why we need All Files Access then open the settings screen
        MaterialAlertDialogBuilder(context, R.style.DialogStyle)
          .setMessage("Storage access is required for NH-Terminal to function properly. Please grant 'All files access' in system settings.")
          .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
            try {
              val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
              intent.data = ("package:" + context.packageName).toUri()
              context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
              // Fall back to generic settings screen
              try {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                context.startActivity(intent)
              } catch (_: Exception) {
                // nothing we can do programmatically
              }
            }
          }
          .setNegativeButton(android.R.string.cancel, null)
          .show()
        return
      }
      // If we get here, the app already has All files access
      return
    }

    // For Android 6..10 request READ_EXTERNAL_STORAGE at runtime
    if (ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_EXTERNAL_STORAGE,
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(
          context,
          Manifest.permission.READ_EXTERNAL_STORAGE
        )) {
        MaterialAlertDialogBuilder(context, R.style.DialogStyle).setMessage("Please enable Storage permission")
          .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
            doRequestPermission(context, requestCode)
          }.show()

      } else {
        doRequestPermission(context, requestCode)
      }
    }
  }
}

private fun doRequestPermission(context: AppCompatActivity, requestCode: Int) {
  try {
    ActivityCompat.requestPermissions(
      context,
      arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
      requestCode)
  }  catch (_: ActivityNotFoundException) {
     // for MIUI, we ignore it.
   }
}
