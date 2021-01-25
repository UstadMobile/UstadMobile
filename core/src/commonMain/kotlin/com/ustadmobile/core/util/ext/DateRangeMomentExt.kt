package com.ustadmobile.core.util.ext

import com.soywiz.klock.DateTime
import com.ustadmobile.lib.db.entities.DateRangeMoment
import com.ustadmobile.lib.db.entities.Moment
import kotlin.math.abs

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