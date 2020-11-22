package com.ustadmobile.core.util.ext

import java.io.File


/**
 * Checks to see if there is a valid command here. On Linux this is the same as file.exists(). On windows, this will look for the file itself as well as variants with .exe and .bat
 */
fun File.commandExists(): Boolean {

    val osName = System.getProperty("os.name")

    // checks linux
    if(osName.contains("nix") || osName.contains("nux") || osName.contains("aix")){
        return exists()
        // checks windows
    }else if(osName.contains("win")){
        return File(parent,"$name.exe").exists() || File(parent,"$name.bat").exists()
    }
    return false
}