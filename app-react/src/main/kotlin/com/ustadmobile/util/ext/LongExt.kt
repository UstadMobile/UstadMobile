package com.ustadmobile.util.ext

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.moment
import kotlin.js.Date

/**
 * Max date that can be represented on JS
 */
val MAX_VALUE: Long
    get() = 8640000000000000L

fun Long?.toDate(emptyIfMaxValue: Boolean = false): Date? {
    return when {
        this ?: 0L == 0L -> null
        this == Long.MAX_VALUE -> if(emptyIfMaxValue) null else Date(MAX_VALUE)
        this == null -> null
        else -> Date(this)
    }
}

fun Long?.formatToStringHoursMinutesSeconds(impl: UstadMobileSystemImpl): String {
    val context = Any()
    val instance = moment.duration(this ?: 0)
    val hours = instance.hours().toString().toInt()
    val minutes = instance.minutes().toString().toInt()
    val seconds = instance.seconds().toString().toInt()
    var result = ""
    if(hours > 0){
        result += "$hours${impl.getString(MessageID.xapi_hours, context)}"
    }

    if(minutes > 0){
        result += "$minutes${impl.getString(MessageID.xapi_minutes, context)}"
    }

    if(seconds > 0){
        result += "$seconds${impl.getString(MessageID.xapi_seconds, context)}"
    }

    return result
}

fun Long?.isSetDate(): Boolean {
    return this != null && this != 0L && this != 8640000000000000
}
