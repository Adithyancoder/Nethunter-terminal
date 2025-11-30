package com.offsec.nhterm.ui.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.appbar.MaterialToolbar
import com.offsec.nhterm.R

class SettingActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ui_settings)
        //val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        //setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState == null) {
            val fragment = when (intent.getStringExtra(EXTRA_SHOW_FRAGMENT)) {
                GeneralSettingsFragment::class.java.name -> GeneralSettingsFragment()
                UISettingsFragment::class.java.name -> UISettingsFragment()
                else -> RootSettingsFragment()
            }
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, fragment)
                .commit()
        }
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment!!
        )
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, fragment)
            .addToBackStack(null)
            .commit()
        return true
        }
      override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
          onBackPressedDispatcher.onBackPressed()
        return true
      }
    return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_SHOW_FRAGMENT = ":android:show_fragment"
    }
}
