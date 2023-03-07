package com.ustadmobile.core.util.ext

import java.io.File
import kotlin.text.lowercase

/**
 * Find a file that implements a command for the receiver's name. On Unix/Linux, this is simple, it
 * is just the file itself (if it exists).
 *
 * If in Windows, we need to look for the file with a .exe or .bat extension
 */
fun File.getCommandFile(
    osName: String = System.getProperty("os.name") ?: ""
): File? {
    val isWin = osName.lowercase().contains("win")
    return when {
        exists() -> this
        isWin && File(parent,"$name.exe").exists() -> File(parent,"$name.exe")
        isWin && File(parent, "$name.bat").exists() -> File(parent,"$name.bat")
        else -> null
    }
}

/**
 * Checks to see if there is a valid command here. On Linux this is the same as file.exists(). On windows, this will look for the file itself as well as variants with .exe and .bat
 */
fun File.commandExists(
    osName: String = System.getProperty("os.name") ?: ""
) = this.getCommandFile(osName) != null
