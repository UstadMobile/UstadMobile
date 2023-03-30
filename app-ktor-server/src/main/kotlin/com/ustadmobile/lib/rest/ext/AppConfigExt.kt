package com.ustadmobile.lib.rest.ext

import com.ustadmobile.lib.rest.CONF_DBMODE_SINGLETON
import io.ktor.server.config.*

fun ApplicationConfig.dbModeProperty(): String {
    return propertyOrNull("ktor.ustad.dbmode")?.getString() ?: CONF_DBMODE_SINGLETON
}
