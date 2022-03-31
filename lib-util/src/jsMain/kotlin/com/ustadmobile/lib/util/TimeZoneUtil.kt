package com.ustadmobile.lib.util

/**
 * Platform specific method to get the system timezone ID from the system
 */
actual fun getDefaultTimeZoneId(): String = js("Intl.DateTimeFormat().resolvedOptions().timeZone") as String
