package com.offsec.nhterm.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.offsec.nhterm.R

class GeneralSettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting_general, rootKey)
    }
}
