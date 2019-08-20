package com.ustadmobile.core.util

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import com.soywiz.klock.Month
import com.soywiz.klock.Year

/**
 * Basic calendar related utility methods. These are isolated in their own class as Calendar is not
 * supported on the GWT client.
 */
object UMCalendarUtil {

    private val httpDateFormat: DateFormat = DateFormat("EEE, dd MMM yyyy HH:mm:ss z")
    private val httpDateFormat2: DateFormat = DateFormat("EEE, dd-MMM-yyyy HH:mm:ss z")
    private val iso8601DateFormat: DateFormat = DateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

    private val listOfFormats = listOf(httpDateFormat, httpDateFormat2, iso8601DateFormat)

    /**
     * Make a String for the date given by time as an HTTP Date as per
     * http://tools.ietf.org/html/rfc2616#section-3.3
     *
     * e.g.
     * Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
     *
     * @param time The time to generate the date for
     * @return A string with a properly formatted HTTP Date
     */
    fun makeHTTPDate(time: Long): String {
        val cal = DateTime(time)
        return cal.format(httpDateFormat)
    }

    /**
     * Parse the ISO 8601 combined date and time format string
     *
     * e.g.
     * 2016-04-18T17:08:07.563789+00:00
     *
     */
    fun parse8601Timestamp(timestamp: String): Long {
        val date = DateTime(
                Year(timestamp.substring(0, 4).toInt()),
                Month(timestamp.substring(5, 7).toInt()),
                timestamp.substring(8, 10).toInt(),
                timestamp.substring(11,13).toInt(),
                timestamp.substring(14,16).toInt(),
                timestamp.substring(17,19).toInt(),
                0)
        return date.unixMillisLong
    }


    /**
     * Appends two digits for the integer i; if i < 10; prepend a leading 0
     *
     * @param i Numbe to append
     * @param sb StringBuffer to append it two
     * @return The stringbuffer
     */
    private fun appendTwoDigits(i: Int, sb: StringBuilder): StringBuilder {
        if (i < 10) {
            sb.append('0')
        }
        sb.append(i)

        return sb
    }

    private fun checkYear(year: Int): Int {
        return if (year < 30) {
            2000 + year
        } else if (year < 100) {
            1900 + year
        } else {
            year
        }
    }

    fun setDate(year: Int, month: Int, dayOfMonth: Int): DateTime {
        return DateTime(year, month, dayOfMonth)
    }

}
