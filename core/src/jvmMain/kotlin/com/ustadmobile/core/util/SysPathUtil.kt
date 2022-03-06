package com.ustadmobile.core.util

import java.io.File
import com.ustadmobile.core.util.ext.getCommandFile

object SysPathUtil {

    fun findCommandInPath(
        commandName: String,
        manuallySpecifiedLocation: File? = null,
        pathVar: String = System.getenv("PATH") ?: "",
        extraSearchPaths: String = File(".").absolutePath,
        osName: String = System.getProperty("os.name") ?: "",
        fileSeparator: String = File.pathSeparator,
    ): File? {
        if(manuallySpecifiedLocation?.exists() == true)
            return manuallySpecifiedLocation

        val pathToSearch = pathVar + if(extraSearchPaths.isNotEmpty()) {
            fileSeparator + extraSearchPaths
        }else {
            ""
        }

        return pathToSearch.split(fileSeparator).mapNotNull {
            File(it, commandName).getCommandFile(osName)
        }.firstOrNull()
    }

    fun commandExists(
        commandName: String,
        manuallySpecifiedLocation: File? = null,
        pathVar: String = System.getenv("PATH") ?: "",
        osName: String = System.getProperty("os.name") ?: "",
        fileSeparator: String = File.pathSeparator
    ) : Boolean {
        return (manuallySpecifiedLocation?.exists() ?: false) ||
                findCommandInPath(commandName, manuallySpecifiedLocation, pathVar, osName,
                    fileSeparator) != null
    }

}