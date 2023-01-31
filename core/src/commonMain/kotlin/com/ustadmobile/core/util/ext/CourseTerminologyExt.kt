package com.ustadmobile.core.util.ext

import com.ustadmobile.core.controller.TerminologyKeys
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.lib.db.entities.TerminologyEntry
import kotlinx.serialization.json.Json

fun CourseTerminology?.toTermMap(
    json: Json,
    systemImpl: UstadMobileSystemImpl
): Map<String, String> {
    val defaultMap = TerminologyKeys.TERMINOLOGY_ENTRY_MESSAGE_ID
        .mapValues { systemImpl.getString(it.value) }

    val map = (this?.ctTerminology?.let {  json.decodeStringMapFromString(it) } ?: defaultMap)
        .toMutableMap()

    // check if missing any default values
    defaultMap.forEach {
        map[it.key] = map[it.key] ?: it.value
    }
    return map
}

/**
 * Read the Json map on the CourseTerminology entity and return a list of TerminologyEntry. If the
 * entry is not present, then use the default value from strings xml.
 */
fun CourseTerminology?.toTerminologyEntries(
    json: Json,
    systemImpl: UstadMobileSystemImpl,
): List<TerminologyEntry> {
    val termMap =  this?.ctTerminology?.let { json.decodeStringMapFromString(it) } ?: mapOf()

    return TerminologyKeys.TERMINOLOGY_ENTRY_MESSAGE_ID.map {
        TerminologyEntry(it.key, it.value, termMap[it.key] ?: systemImpl.getString(it.value))
    }
}
