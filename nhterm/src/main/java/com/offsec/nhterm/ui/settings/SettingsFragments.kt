package com.offsec.nhterm.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.offsec.nhterm.R

class MainSettingsFragment : PreferenceFragmentCompat() {
  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) R.xml.settings_main else R.xml.older_settings_main,
      rootKey
    )
  }
}

class GeneralSettingsFragment : PreferenceFragmentCompat() {
  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.setting_general, rootKey)
  }
}

class UISettingsFragment : PreferenceFragmentCompat() {
  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.settings_ui, rootKey)
  }
}
