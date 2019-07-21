package com.ustadmobile.core.util

import com.soywiz.klock.*

/**
 * Basic calendar related utility methods. These are isolated in their own class as Calendar is not
 * supported on the GWT client.
 */
object UMCalendarUtil {

    private val httpDateFormat: DateFormat = DateFormat("EEE, dd MMM yyyy HH:mm:ss z")
    private val httpDateFormat2: DateFormat = DateFormat("EEE, dd-MMM-yyyy HH:mm:ss z")
    private val iso8601DateFormat: DateFormat = ISO8601.DATE_CALENDAR_COMPLETE

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
        return iso8601DateFormat.parse(timestamp).local.unixMillisLong
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

    /**
    * Gets date in long plus/minus the days specified from today.
    *
    * @param days  The days (positive or negative) off from today
    * @return  The date in long
    */
    public fun getDateInMilliPlusDays(nDays: Int):Long {
        val now = DateTime.now()
        val duration = nDays.days
        val then = now + duration
        return then.unixMillisLong
    }

    /**
     * Gets simple pretty looking date (eg; 23/Jan/89) from a long date specified.
     *
     * @param thisDate  The date in long for which we want a pretty simple date.
     * @return  The pretty simple date for the long date specified as string.
     */
     fun getPrettyDateSuperSimpleFromLong(thisDate: Long):String{

        val format: DateFormat = DateFormat("dd/MMM/yyyy")
        val cal = DateTime(thisDate)
        return cal.format(httpDateFormat)
    }

    fun getPrettyDateSuperSimpleFromLong(thisDate: Long, locale: Any?):String{
        return getPrettyDateSuperSimpleFromLong(thisDate)
    }

    /***
     * Returns date as 23/Jan/1989
     * @param thisDate  Long date
     * @param locale    Locale
     * @return          String "23/Jan/1989"
     */
    fun getPrettyDateSimpleFromLong(thisDate:Long,locale:Any?):String{
        return getPrettyDateSuperSimpleFromLong(thisDate)
    }
}
