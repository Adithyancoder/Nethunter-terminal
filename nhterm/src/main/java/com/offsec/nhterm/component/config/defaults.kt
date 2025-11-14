package com.offsec.nhterm.component.config

import android.annotation.SuppressLint

object DefaultValues {
  const val FONT_SIZE = 18
  const val ENABLE_BELL = false
  const val ENABLE_VIBRATE = false
  val enableExecveWrapper = true
  val enableAutoCompletion = false
  val enableFullScreen = false
  val enableAutoHideToolbar = false
  val enableSwitchNextTab = false
  val enableExtraKeys = true
  val enableExplicitExtraKeysWeight = false
  val enableBackButtonBeMappedToEscape = false
  val enableSpecialVolumeKeys = false
  val enableWordBasedIme = false
  const val LOGIN_SHELL = "bash"
  const val INITIAL_COMMAND = ""
  const val DEFAULT_FONT = "FiraCode"
}

object NeoTermPath {
  @SuppressLint("SdCardPath")
  const val ROOT_PATH = "/data/data/com.offsec.nhterm/files"
  const val USR_PATH = "$ROOT_PATH/usr"
  const val BIN_PATH = "$USR_PATH/bin"
  const val HOME_PATH = "/"
  const val APT_BIN_PATH = "$USR_PATH/bin/apt"
  const val LIB_PATH = "$USR_PATH/lib"
  const val CUSTOM_PATH = "$ROOT_PATH/usr/home/.nhterm"
  const val NEOTERM_LOGIN_SHELL_PATH = "$CUSTOM_PATH/shell"
  const val EKS_PATH = "$CUSTOM_PATH/eks"
  const val EKS_DEFAULT_FILE = "$EKS_PATH/default.nl"
  const val FONT_PATH = "$CUSTOM_PATH/font"
  const val COLORS_PATH = "$CUSTOM_PATH/color"
  const val USER_SCRIPT_PATH = "$CUSTOM_PATH/script"
  const val PROFILE_PATH = "$CUSTOM_PATH/profile"
  const val SOURCE_FILE = "$USR_PATH/etc/apt/sources.list"
  const val PACKAGE_LIST_DIR = "$USR_PATH/var/lib/apt/lists"
  private const val SOURCE = "http://http.kali.org/kali"
  const val DEFAULT_MAIN_PACKAGE_SOURCE: String = SOURCE
}
