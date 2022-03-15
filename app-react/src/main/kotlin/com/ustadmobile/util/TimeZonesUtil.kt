package com.ustadmobile.util

import kotlinx.serialization.Serializable

@JsModule("timezones.json")
@JsNonModule
external val timezoneModule: dynamic

@Serializable
data class TimeZone(
    val timeName: String, val name: String,
    val offset: Double, val abbreviation: String,
    val isDst: Boolean, val id: String)

object TimeZonesUtil {
    fun getTimeZones(): List<TimeZone>{
        val timeZoneList = mutableListOf<TimeZone>()
        timezoneModule.forEach { zone ->
            zone.utc.join(",").toString().split(",").forEachIndexed { index, utc ->
                if(index != 0){
                    val name = zone.text.toString().substringBefore(")") + ")"
                    timeZoneList.add(TimeZone(name,
                        "$name $utc",
                        "${zone.offset}".toDouble(), zone.abbr, "${zone.isdst}".toBoolean(), utc))
                }
            }
        }
        return timeZoneList.toList()
    }
}

