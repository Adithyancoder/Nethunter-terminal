package com.offsec.nhterm.ui.settings

import android.os.Build
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.offsec.nhterm.R

class RootSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setPreferencesFromResource(R.xml.settings_main, rootKey)
        } else {
            setPreferencesFromResource(R.xml.older_settings_main, rootKey)
        }
    }
}
