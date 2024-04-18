package com.ustadmobile.core.util.ext

import java.io.File
import kotlin.text.lowercase

fun isWindowsOs(osName: String = System.getProperty("os.name") ?: ""): Boolean {
    return osName.lowercase().contains("win")
}

fun isLinuxOs(osName: String = System.getProperty("os.name") ?: ""): Boolean {
    return osName.lowercase().let { it.contains("linux") || it.contains("nix") }
}

/**
 * Find a file that implements a command for the receiver's name. On Unix/Linux, this is simple, it
 * is just the file itself (if it exists).
 *
 * If in Windows, we need to look for the file with a .exe or .bat extension
 */
fun File.getCommandFile(
    osName: String = System.getProperty("os.name") ?: ""
): File? {
    val isWin = isWindowsOs(osName)
    return when {
        exists() -> this
        isWin && File(parent,"$name.exe").exists() -> File(parent,"$name.exe")
        isWin && File(parent, "$name.bat").exists() -> File(parent,"$name.bat")
        else -> null
    }
}
