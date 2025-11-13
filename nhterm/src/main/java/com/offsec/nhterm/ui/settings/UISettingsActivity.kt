package com.offsec.nhterm.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.offsec.nhterm.R

/**
 * @author kiva
 */
@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class UISettingsActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar?.title = getString(R.string.ui_settings)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    supportFragmentManager
      .beginTransaction()
      .replace(android.R.id.content, UISettingsFragment())
      .commit()
  }
}
