package com.ustadmobile.lib.rest.ext

import com.ustadmobile.lib.rest.CONF_DBMODE_SINGLETON
import io.ktor.server.request.*
import java.util.*
import com.ustadmobile.core.account.Endpoint
import io.ktor.server.application.*
import io.ktor.server.config.*
import java.io.File

/**
 * Create a Properties object from a HOCON config section.
 */
fun ApplicationConfig.toProperties(propertyNames: List<String>): Properties {
    val props = Properties()

    propertyNames.forEach { propName ->
        this.propertyOrNull(propName)?.also { propVal ->
            props.setProperty(propName, propVal.getString())
        }
    }

    return props
}


/**
 * Given a section in the KTOR HOCON config that contains driver, url, user, and password, turn this
 * into a Properties that can be used to initialize a DataSource
 */
fun ApplicationConfig.databasePropertiesFromSection(
    section: String,
    defaultUrl : String
) : Properties {
    return Properties().apply {
        setProperty("driver",
            propertyOrNull("$section.driver")?.getString() ?: "org.sqlite.JDBC")
        setProperty("url",
            propertyOrNull("$section.url")?.getString() ?: defaultUrl)
        setProperty("user",
            propertyOrNull("$section.user")?.getString() ?: "")
        setProperty("password",
            propertyOrNull("$section.password")?.getString() ?: "")
    }
}

fun ApplicationConfig.dbModeToEndpoint(call: ApplicationCall, dbModeOverride: String? = null): Endpoint{
    val dbMode: String = dbModeOverride ?: propertyOrNull("ktor.ustad.dbmode")?.getString() ?: CONF_DBMODE_SINGLETON
    return if(dbMode == CONF_DBMODE_SINGLETON) {
        Endpoint("localhost")
    }else {
        Endpoint(call.request.header("Host") ?: "localhost")
    }
}

/**
 * Get a file for an external command e.g. ffmpeg etc specified in the paths section
 */
fun ApplicationConfig.commandFileProperty(command: String) : File? {
    return propertyOrNull("ktor.ustad.paths.$command")?.getString()?.let { File(it) }
}
