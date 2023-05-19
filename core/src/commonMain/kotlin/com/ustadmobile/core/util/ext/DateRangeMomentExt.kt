package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.DateRangeMoment
import com.ustadmobile.lib.db.entities.Moment
import kotlinx.datetime.*
import kotlin.math.abs

fun DateRangeMoment.toFixedDatePair(): Pair<Long, Long>{

    val fromDate = fromMoment.toFixedDate()
    val toDate = toMoment.toFixedDate()

    return Pair(fromDate, toDate)
}

fun Moment.toFixedDate(): Long{
    if(typeFlag == Moment.TYPE_FLAG_FIXED){
        return fixedTime
    }else{

        if(relUnit == Moment.DAYS_REL_UNIT && relOffSet == -0){
            return Clock.System.now().toLocalMidnight(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()
        }

        val dateTimeUnit = when(relUnit){
            Moment.DAYS_REL_UNIT -> DateTimeUnit.DAY
            Moment.MONTHS_REL_UNIT -> DateTimeUnit.MONTH
            Moment.WEEKS_REL_UNIT -> DateTimeUnit.WEEK
            Moment.YEARS_REL_UNIT -> DateTimeUnit.YEAR
            else -> throw IllegalArgumentException("relUnit not a valid Moment. REL UNIT constnat")
        }

        return Clock.System.now().minus(relOffSet, dateTimeUnit, TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
    }
}

fun DateRangeMoment.toDisplayString(): String{
    var str = "Custom range: from "
    str += fromMoment.toDisplayString()
    str += " until ${toMoment.toDisplayString()}"

    return str
}

fun Moment.toDisplayString(): String{

    var str = ""
    when(typeFlag){

        Moment.TYPE_FLAG_FIXED -> {
            val dateTime = Instant.fromEpochMilliseconds(fixedTime)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            str += "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year}"
        }

        Moment.TYPE_FLAG_RELATIVE ->{

            if(relUnit == Moment.DAYS_REL_UNIT && relOffSet == -0){
                str += "now"
            }else{

                str += abs(relOffSet).toString()

                when(relUnit){

                    Moment.DAYS_REL_UNIT -> str += " days ago"
                    Moment.MONTHS_REL_UNIT -> str += " months ago"
                    Moment.WEEKS_REL_UNIT -> str += " weeks ago"
                    Moment.YEARS_REL_UNIT -> str += " years ago"

                }

            }
        }
    }

    return str
}