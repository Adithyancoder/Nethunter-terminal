package com.offsec.nhterm.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.offsec.nhterm.R

/**
 * A lightweight base activity for settings-related screens.
 * Replaces the old PreferenceActivity-based implementation to avoid using deprecated APIs.
 */
abstract class BasePreferenceActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Ensure the app theme is applied for subclasses
    setTheme(R.style.AppTheme)
  }
}
