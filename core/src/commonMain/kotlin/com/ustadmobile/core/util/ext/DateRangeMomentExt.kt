package com.ustadmobile.core.util.ext

import com.soywiz.klock.*
import com.ustadmobile.lib.db.entities.DateRangeMoment
import com.ustadmobile.lib.db.entities.Moment
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
            return DateTime.nowUnixLong()
        }

        val timeNow = DateTime.now().startOfDay
        val timeOffset = when(relUnit){
            Moment.DAYS_REL_UNIT -> timeNow - abs(relOffSet).days
            Moment.MONTHS_REL_UNIT -> timeNow - abs(relOffSet).months
            Moment.WEEKS_REL_UNIT -> timeNow - abs(relOffSet).weeks
            Moment.YEARS_REL_UNIT -> timeNow - abs(relOffSet).years
            else -> timeNow
        }
        return timeOffset.unixMillisLong
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

            str += DateTime(fixedTime).format("dd/MM/yyyy")

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