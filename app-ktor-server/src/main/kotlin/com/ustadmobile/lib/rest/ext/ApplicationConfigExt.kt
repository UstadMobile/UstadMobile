package com.ustadmobile.lib.rest.ext

import java.util.*
import com.ustadmobile.lib.rest.CONF_KEY_SITE_URL
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
    defaultUrl : String,
    defaultDriver: String = "org.sqlite.JDBC",
    defaultUser: String = "",
) : Properties {
    return Properties().apply {
        setProperty("driver",
            propertyOrNull("$section.driver")?.getString() ?: defaultDriver)
        setProperty("url",
            propertyOrNull("$section.url")?.getString() ?: defaultUrl)
        setProperty("user",
            propertyOrNull("$section.user")?.getString() ?: defaultUser)
        setProperty("password",
            propertyOrNull("$section.password")?.getString() ?: "")
    }
}

/**
 * Get a file for an external command e.g. ffmpeg etc specified in the paths section
 */
fun ApplicationConfig.commandFileProperty(command: String) : File? {
    return propertyOrNull("ktor.ustad.paths.$command")?.getString()?.let { File(it) }
}

fun ApplicationConfig.siteUrl() = propertyOrNull(CONF_KEY_SITE_URL)?.getString() ?: ""
