package com.ustadmobile.util

import kotlinx.serialization.Serializable

@JsModule("timezones.json")
@JsNonModule
external val timezoneModule: dynamic

@Serializable
data class TimeZone(
    val timeName: String, val name: String,
    val offset: Int, val abbreviation: String,
    val isDst: Boolean, val id: String)

object TimeZonesUtil {
    fun getTimeZones(): List<TimeZone>{
        val timeZoneList = mutableListOf<TimeZone>()
        timezoneModule.forEach { zone ->
            zone.utc.join(",").toString().split(",").forEach { utc ->
                timeZoneList.add(TimeZone(zone.value, zone.text,
                    zone.offset, zone.abbr, zone.isdst, utc))
            }
        }
        return timeZoneList.toList()
    }
}

