package com.offsec.nhterm.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.offsec.nhterm.R

/**
 * @author Lody
 */
class SettingActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar?.title = getString(R.string.settings)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    // Host the MainSettingsFragment
    supportFragmentManager
      .beginTransaction()
      .replace(android.R.id.content, MainSettingsFragment())
      .commit()
  }
}
