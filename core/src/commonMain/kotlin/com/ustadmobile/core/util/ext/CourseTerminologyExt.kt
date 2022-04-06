package com.ustadmobile.core.util.ext

import com.ustadmobile.core.controller.TerminologyKeys
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.CourseTerminology
import kotlinx.serialization.json.Json

fun CourseTerminology?.toTermMap(json: Json, systemImpl: UstadMobileSystemImpl, context: Any): Map<String, String> {
    val defaultMap = TerminologyKeys.TERMINOLOGY_ENTRY_MESSAGE_ID
        .mapValues { systemImpl.getString(it.value, context) }

    val map = (this?.ctTerminology?.let {  json.decodeStringMapFromString(it) } ?: defaultMap)
        .toMutableMap()

    // check if missing any default values
    defaultMap.forEach {
        map[it.key] = map[it.key] ?: it.value
    }
    return map
}