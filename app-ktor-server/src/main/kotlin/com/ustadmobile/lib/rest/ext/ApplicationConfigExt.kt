package com.ustadmobile.lib.rest.ext

import io.ktor.config.*
import java.util.*

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
fun ApplicationConfig.databasePropertiesFromSection(section: String,
                                                    defaultUrl : String) : Properties {
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
