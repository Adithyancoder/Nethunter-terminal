package com.offsec.nhterm.ui.customize

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.offsec.nhterm.R
import com.offsec.nhterm.component.ComponentManager
import com.offsec.nhterm.component.colorscheme.ColorSchemeComponent
import com.offsec.nhterm.component.config.NeoTermPath
import com.offsec.nhterm.component.font.FontComponent
import com.offsec.nhterm.utils.getPathOfMediaUri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * @author kiva
 */
class CustomizeActivity : BaseCustomizeActivity() {
  // Activity Result launchers for selecting files
  private lateinit var selectFontLauncher: ActivityResultLauncher<Intent>
  private lateinit var selectColorLauncher: ActivityResultLauncher<Intent>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initCustomizationComponent(R.layout.ui_customize)

    // Register launchers
    selectFontLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK && result.data != null) {
        val uri = result.data!!.data
        if (uri != null) {
          // Prefer filesystem path when available, otherwise copy from content URI
          val selected = this.getPathOfMediaUri(uri)
          if (!selected.isNullOrEmpty() && File(selected).exists()) {
            installFont(selected)
          } else {
            installFileToUri(uri, NeoTermPath.FONT_PATH)
            setupSpinners()
          }
        }
      }
    }

    selectColorLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK && result.data != null) {
        val uri = result.data!!.data
        if (uri != null) {
          val selected = this.getPathOfMediaUri(uri)
          if (!selected.isNullOrEmpty() && File(selected).exists()) {
            installColor(selected)
          } else {
            installFileToUri(uri, NeoTermPath.COLORS_PATH)
            setupSpinners()
          }
        }
      }
    }

    findViewById<View>(R.id.custom_install_font_button).setOnClickListener {
      val intent = Intent()
      intent.action = Intent.ACTION_GET_CONTENT
      intent.type = "*/*"
      selectFontLauncher.launch(Intent.createChooser(intent, getString(R.string.install_font)))
    }

    findViewById<View>(R.id.custom_install_color_button).setOnClickListener {
      val intent = Intent()
      intent.action = Intent.ACTION_GET_CONTENT
      intent.type = "*/*"
      selectColorLauncher.launch(
        Intent.createChooser(intent, getString(R.string.install_color))
      )
    }
  }

  private fun setupSpinners() {
    val fontComponent = ComponentManager.getComponent<FontComponent>()
    val colorSchemeComponent = ComponentManager.getComponent<ColorSchemeComponent>()

    setupSpinner(R.id.custom_font_spinner, fontComponent.getFontNames(),
      fontComponent.getCurrentFontName(), object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
          val fontName = parent!!.adapter!!.getItem(position) as String
          val font = fontComponent.getFont(fontName)
          fontComponent.applyFont(terminalView, extraKeysView, font)
          fontComponent.setCurrentFont(fontName)
        }
      })

    val colorData = listOf(
      getString(R.string.new_color_scheme),
      *colorSchemeComponent.getColorSchemeNames().toTypedArray()
    )
    setupSpinner(R.id.custom_color_spinner, colorData,
      colorSchemeComponent.getCurrentColorSchemeName(), object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
          if (position == 0) {
            val intent = Intent(this@CustomizeActivity, ColorSchemeActivity::class.java)
            startActivity(intent)
            return
          }
          val colorName = parent!!.adapter!!.getItem(position) as String
          val color = colorSchemeComponent.getColorScheme(colorName)
          colorSchemeComponent.applyColorScheme(terminalView, extraKeysView, color)
          colorSchemeComponent.setCurrentColorScheme(colorName)
        }
      })
  }

  private fun setupSpinner(
    id: Int,
    data: List<String>,
    selected: String,
    listener: AdapterView.OnItemSelectedListener
  ): Spinner {
    val spinner = findViewById<Spinner>(id)
    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, data)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    spinner.adapter = adapter
    spinner.onItemSelectedListener = listener
    spinner.setSelection(if (data.contains(selected)) data.indexOf(selected) else 0)
    return spinner
  }

  override fun onResume() {
    super.onResume()
    setupSpinners()
  }

  override fun onDestroy() {
    super.onDestroy()
    session.finishIfRunning()
  }

  private fun installColor(selected: String) {
    installFileTo(selected, NeoTermPath.COLORS_PATH)
    setupSpinners()
  }

  private fun installFont(selected: String) {
    installFileTo(selected, NeoTermPath.FONT_PATH)
    setupSpinners()
  }

  private fun installFileTo(file: String, targetDir: String) {
    kotlin.runCatching {
      val source = File(file)
      val targetDirFile = File(targetDir)
      if (!targetDirFile.exists()) {
        targetDirFile.mkdirs()
      }
      val dest = File(targetDirFile, source.name)

      FileInputStream(source).use { input ->
        FileOutputStream(dest).use { output ->
          val buffer = ByteArray(8 * 1024)
          var bytesRead: Int
          while (true) {
            bytesRead = input.read(buffer)
            if (bytesRead <= 0) break
            output.write(buffer, 0, bytesRead)
          }
          output.flush()
        }
      }
    }.onFailure {
      Toast.makeText(this, getString(R.string.error) + ": ${it.localizedMessage}", Toast.LENGTH_LONG).show()
    }
  }

  private fun installFileToUri(uri: Uri, targetDir: String) {
    kotlin.runCatching {
      val targetDirFile = File(targetDir)
      if (!targetDirFile.exists()) targetDirFile.mkdirs()
      val dest = File(targetDirFile, File(uri.lastPathSegment ?: "imported").name)

      contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(dest).use { output ->
          val buffer = ByteArray(8 * 1024)
          var bytesRead: Int
          while (true) {
            bytesRead = input.read(buffer)
            if (bytesRead <= 0) break
            output.write(buffer, 0, bytesRead)
          }
          output.flush()
        }
      } ?: throw IOException("Unable to open input stream for URI: $uri")
    }.onFailure {
      Toast.makeText(this, getString(R.string.error) + ": ${it.localizedMessage}", Toast.LENGTH_LONG).show()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> finish()
    }
    return item.let { super.onOptionsItemSelected(it) }
  }
}
