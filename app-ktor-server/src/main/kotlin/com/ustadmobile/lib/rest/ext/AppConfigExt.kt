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
 * The FFMPEG dir within the app directory itself (e.g. installpath/ffmpeg). This will be searched by
 * default for ffmpeg and ffprobe
 */
fun ktorAppHomeFfmpegDir(): File = File(ktorAppHomeDir(), "ffmpeg")


fun ApplicationConfig.absoluteDataDir(): File {
    val dataDirPropValue = propertyOrNull("ktor.ustad.datadir")?.getString() ?: "data"
    val dataDirConf = File(dataDirPropValue)

    return if(dataDirConf.isAbsolute) {
        dataDirConf
    }else {
        File(ktorAppHomeDir(), dataDirPropValue)
    }
}

