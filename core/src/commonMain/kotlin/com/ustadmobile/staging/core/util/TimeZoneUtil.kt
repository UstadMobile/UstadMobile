package com.ustadmobile.staging.core.util

expect class TimeZoneUtil{

    fun getDeviceTimezone(): String

    fun getPrettyDateWithTimeFromLongSimple(thisDate: Long, locale: String): String

    constructor()
}