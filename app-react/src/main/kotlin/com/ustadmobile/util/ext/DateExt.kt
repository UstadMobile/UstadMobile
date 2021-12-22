package com.ustadmobile.util.ext

import com.ustadmobile.core.util.moment
import kotlin.js.Date

fun Date.formatDate(format: String = "DD/MMMM/YYYY"): String {
    val utc = moment.utc(this).toDate()
    return moment(utc).local().format(format)
}


fun Date.standardFormat(): String = formatDate("DD/MM/YYYY")

fun Date.formattedInHoursAndMinutes() : String = formatDate("HH:mm")

fun Date.formattedWithFullMonth(): String = formatDate("DD/MMMM/YYYY")