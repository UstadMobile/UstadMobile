package com.ustadmobile.lib.rest.ext

import io.ktor.config.*
import java.util.*

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
            propertyOrNull("ktor.database.user")?.getString() ?: "")
        setProperty("password",
            propertyOrNull("ktor.database.password")?.getString() ?: "")
    }
}