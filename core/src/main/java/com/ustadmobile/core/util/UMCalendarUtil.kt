package com.ustadmobile.core.util

import com.ustadmobile.lib.util.UMUtil

import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.Vector

/**
 * Basic calendar related utility methods. These are isolated in their own class as Calendar is not
 * supported on the GWT client.
 */
object UMCalendarUtil {

    val HTTP_MONTH_NAMES = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    val HTTP_DAYS = intArrayOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)

    val HTTP_DAY_LABELS = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")


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
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        cal.time = Date(time)
        val sb = StringBuffer()

        val `val` = cal.get(Calendar.DAY_OF_WEEK)
        for (i in HTTP_MONTH_NAMES.indices) {
            if (`val` == HTTP_DAYS[i]) {
                sb.append(HTTP_DAY_LABELS[i]).append(", ")
                break
            }
        }
        appendTwoDigits(cal.get(Calendar.DAY_OF_MONTH), sb).append(' ')

        sb.append(HTTP_MONTH_NAMES[cal.get(Calendar.MONTH)]).append(' ')
        sb.append(checkYear(cal.get(Calendar.YEAR))).append(' ')
        appendTwoDigits(cal.get(Calendar.HOUR_OF_DAY), sb).append(':')
        appendTwoDigits(cal.get(Calendar.MINUTE), sb).append(':')
        appendTwoDigits(cal.get(Calendar.SECOND), sb).append(" GMT")

        return sb.toString()
    }

    /**
     * Parse the given http date according to :
     * http://tools.ietf.org/html/rfc2616#section-3.3
     *
     * @param httpDate
     * @return
     */
    fun parseHTTPDate(httpDate: String): Long {
        val delimChars = charArrayOf(' ', ':', '-')

        val tokens = UMUtil.tokenize(httpDate, delimChars, 0, httpDate.length)
        val cal: Calendar?

        if (tokens.size == 8) {//this includes the timezone
            cal = Calendar.getInstance(TimeZone.getTimeZone(
                    tokens.elementAt(7) as String))
            cal!!.set(Calendar.DAY_OF_MONTH, Integer.parseInt(
                    tokens.elementAt(1) as String))
            cal.set(Calendar.MONTH, UMUtil.getIndexInArrayIgnoreCase(
                    tokens.elementAt(2) as String, HTTP_MONTH_NAMES))
            cal.set(Calendar.YEAR, checkYear(Integer.parseInt(
                    tokens.elementAt(3) as String)))

            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(
                    tokens.elementAt(4) as String))
            cal.set(Calendar.MINUTE, Integer.parseInt(
                    tokens.elementAt(5) as String))
            cal.set(Calendar.SECOND, Integer.parseInt(
                    tokens.elementAt(6) as String))
        } else if (tokens.size == 7) {
            cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
            cal!!.set(Calendar.MONTH, UMUtil.getIndexInArrayIgnoreCase(
                    tokens.elementAt(1) as String, HTTP_MONTH_NAMES))
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(
                    tokens.elementAt(2) as String))
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(
                    tokens.elementAt(3) as String))
            cal.set(Calendar.MINUTE, Integer.parseInt(
                    tokens.elementAt(4) as String))
            cal.set(Calendar.SECOND, Integer.parseInt(
                    tokens.elementAt(5) as String))

            cal.set(Calendar.YEAR, checkYear(Integer.parseInt(
                    tokens.elementAt(6) as String)))
        } else {
            return 0L
        }

        return cal.time.time
    }

    /**
     * Parse the ISO 8601 combined date and time format string
     *
     * e.g.
     * 2016-04-18T17:08:07.563789+00:00
     *
     */
    fun parse8601Timestamp(timestamp: String): Calendar {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, Integer.parseInt(timestamp.substring(0, 4)))
        cal.set(Calendar.MONTH, Integer.parseInt(timestamp.substring(5, 7)))
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(timestamp.substring(8, 10)))

        if (timestamp.length < 12) {
            return cal
        }

        //There is a time section
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timestamp.substring(11, 13)))
        cal.set(Calendar.MINUTE, Integer.parseInt(timestamp.substring(14, 16)))
        cal.set(Calendar.SECOND, Integer.parseInt(timestamp.substring(17, 19)))

        return cal
    }


    /**
     * Appends two digits for the integer i; if i < 10; prepend a leading 0
     *
     * @param i Numbe to append
     * @param sb StringBuffer to append it two
     * @return The stringbuffer
     */
    private fun appendTwoDigits(i: Int, sb: StringBuffer): StringBuffer {
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

}
