package com.ustadmobile.core.util

import java.io.File
import com.ustadmobile.core.util.ext.commandExists
import com.ustadmobile.core.util.ext.getCommandFile

object SysPathUtil {

    fun findCommandInPath(
        commandName: String,
        pathVar: String = System.getenv("PATH") ?: "",
        osName: String = System.getProperty("os.name") ?: "",
        fileSeparator: String = File.pathSeparator,
    ): File? {
        return pathVar.split(fileSeparator).mapNotNull {
            File(it, commandName).getCommandFile(osName)
        }.firstOrNull()
    }

    fun commandExists(
        commandName: String,
        manuallySpecifiedLocation: File?,
        pathVar: String = System.getenv("PATH") ?: "",
        osName: String = System.getProperty("os.name") ?: "",
        fileSeparator: String = File.pathSeparator
    ) : Boolean {
        return (manuallySpecifiedLocation?.exists() ?: false) ||
                findCommandInPath(commandName, pathVar, osName, fileSeparator) != null
    }

}