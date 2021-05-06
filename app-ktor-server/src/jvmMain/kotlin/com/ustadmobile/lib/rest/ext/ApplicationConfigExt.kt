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
