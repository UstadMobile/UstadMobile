package com.ustadmobile.lib.rest.ext

import com.ustadmobile.lib.rest.CONF_DBMODE_SINGLETON
import io.ktor.server.config.*
import java.io.File

fun ApplicationConfig.dbModeProperty(): String {
    return propertyOrNull("ktor.ustad.dbmode")?.getString() ?: CONF_DBMODE_SINGLETON
}

//When this is running through the start script created by Gradle, app_home will be set, and we
// will use this as the base directory for any relative path. If not, we will use the working
// directory as the base path.
fun ktorAppHomeDir(): File {
    return System.getProperty("app_home")?.let { File(it) } ?: File(System.getProperty("user.dir"))
}

/**
 * Get a File for a property e.g. for the data directory or well known directory
 *
 * @param propertyName the config property name
 * @param defaultPath default value of property is null
 *
 * @return File object - if the path is absolute, returns the absolute File as is. If not, then will
 * return file relative to the server path as per ktorAppHomeDir()
 */
fun ApplicationConfig.fileProperty(
    propertyName: String, defaultPath: String
): File {
    val path = propertyOrNull(propertyName)?.getString() ?: defaultPath
    val file = File(propertyName)

    return if(file.isAbsolute) {
        file
    }else {
        File(ktorAppHomeDir(), path)
    }
}


fun ApplicationConfig.absoluteDataDir() = fileProperty(
    propertyName = "ktor.ustad.datadir", defaultPath = "data"
)

