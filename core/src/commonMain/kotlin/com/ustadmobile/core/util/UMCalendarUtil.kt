package com.ustadmobile.core.util

import com.soywiz.klock.*

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
     *
     * @param dateString
     * @param format
     * @param locale
     * @return
     */
    fun getLongDateFromStringAndFormat(dateString: String,
                                       format: String,
                                       locale: Any?): Long {

        try{
            val formatted : DateFormat = DateFormat(format)
            val date = formatted.parse(dateString)
            return date.local.unixMillis.toLong()
        }catch (e:DateException){
            return 0
        }

    }


    /**
     * Get date in long w.r.t plus/minus the days specified from a specified date
     * @param dateLong  The specified date (in long) where the days to be calculated.
     * @param days  The days (positive or negative) off from the dateLong specified
     * @return  The date in long
     */
    fun getDateInMilliPlusDaysRelativeTo(dateLong: Long, days: Int): Long {
        // get a calendar instance, which defaults to "now"
        val givenCal = DateTime(dateLong)
        val duration = days.days
        val then = givenCal + duration
        return then.unixMillisLong

    }

    fun getToday000000():Long{
        val cal = DateTime.now()
        val ntcal = DateTime(year = cal.year, month = cal.month, day = cal.dayOfMonth, hour = 0,
                minute = 0, second = 0, milliseconds = 0)
        return ntcal.unixMillisLong
    }

    fun getToday235959():Long{
        val cal = DateTime.now()
        val ntcal = DateTime(year = cal.year, month = cal.month, day = cal.dayOfMonth, hour = 23,
                minute = 59, second = 59)
        return ntcal.unixMillisLong
    }

    /**
     * Checks if a given long date is today or not.
     *
     * @param date  The Date object which we want to check if its a today date.
     *
     * @return  true if given date is today, false if it isn't
     */
    fun isToday(dateLong: Long): Boolean {

        val givenCal = DateTime(dateLong)
        val todayCal = DateTime.now()
        return (givenCal.dayOfYear == todayCal.dayOfYear && givenCal.dayOfMonth == todayCal.dayOfMonth
                && givenCal.dayOfWeekInt == todayCal.dayOfWeekInt)

    }
    fun getDateLongFromYMD(year:Int, month:Int, day:Int):Long{
        val cal = DateTime(year=year, month = month, day = day)
        return cal.unixMillisLong
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
        return cal.format(format)
    }

    fun showTimeForGivenLongDate(thisDate:Long):String{
        val format: DateFormat = DateFormat("HH:mm")
        val cal = DateTime(thisDate)
        return cal.format(format)
    }

    fun zeroOutTimeForGivenLongDate(thisDate: Long):Long{
        val cal = DateTime(thisDate)
        val ntcal = DateTime(year = cal.year, month = cal.month, day = cal.dayOfMonth, hour = 0,
                minute = 0, second = 0, milliseconds = 0)
        return ntcal.unixMillisLong
    }

    fun changeDatetoThis(thisDate:Long, startTimeMins: Long):Long{

        val cal = DateTime(thisDate)
        val ntcal = DateTime(year = cal.year, month = cal.month, day = cal.dayOfMonth,
                hour = (startTimeMins / 60).toInt(), minute = (startTimeMins % 60).toInt(),
                second = 0, milliseconds = 0)
        return ntcal.unixMillisLong
    }

    fun normalizeSecondsAndMillis(thisDate: Long):Long{
        val cal = DateTime(thisDate)
        val ntcal = DateTime(year = cal.year, month = cal.month, day = cal.dayOfMonth, hour = cal.hours,
                minute = cal.minutes, second = 0, milliseconds = 0)
        return ntcal.unixMillisLong
    }

    fun getHourOfDay24(thisDate: Long): Int{
        val cal = DateTime(thisDate)
        return cal.hours
    }

    fun getMinuteOfDay(thisDate: Long): Int{
        val cal = DateTime(thisDate)
        return cal.minutes
    }

    fun getDayOfWeek(thisDate: Long):Int {
        val cal = DateTime(thisDate)
        return cal.dayOfWeekInt
    }

    fun setDayOfWeek(thisDate:Long, dow:Int):Long{
        val cal = DateTime(thisDate)
        val currentDOW = cal.dayOfWeekInt
        if(currentDOW == dow){
            return getDateInMilliPlusDaysRelativeTo(thisDate, 7)
        }
        else if (currentDOW<dow){
            return getDateInMilliPlusDaysRelativeTo(thisDate, dow - currentDOW)
        }else if(currentDOW>dow){
            val plus = 7 - currentDOW + dow
            return getDateInMilliPlusDaysRelativeTo(thisDate, plus)
        }else{
            return getDateInMilliPlusDaysRelativeTo(thisDate, 0)
        }
    }

    fun getPrettyTimeFromLong(thisDate: Long, locale: Any?): String {
        val format: DateFormat = DateFormat("HH:mm")
        val cal = DateTime(thisDate)
        return cal.format(format)
    }

    /**
     * Gets pretty looking date (eg: Mon, 23/Jan/1989) from a long date specified.
     *
     * @param thisDate The date in long for which we want a pretty date
     * @return  The pretty date for the long date specified as string.
     */
    fun getPrettyDateFromLong(thisDate: Long, locale: Any?): String {
        val format: DateFormat = DateFormat("EEE, dd/MMM/yyyy")
        val cal = DateTime(thisDate)
        return cal.format(format)
    }


    /**
     * Gets simple day only (eg: Mon) from a long date specified.
     *
     * @param thisDate  The date in long for which we want the day for.
     * @return  The day for the long date specified as string.
     */
    fun getSimpleDayFromLongDate(thisDate: Long, locale: Any?): String {
        val format: DateFormat = DateFormat("EEEE")
        val cal = DateTime(thisDate)
        return cal.format(format)
    }

    fun getPrettyDateSuperSimpleFromLong(thisDate: Long, locale: Any?):String{
        return getPrettyDateSuperSimpleFromLong(thisDate)
    }

    fun getPrettyDateSimpleWithoutYearFromLong(thisDate: Long, locale: Any?): String {
        val format: DateFormat = DateFormat("dd/MMM")
        val cal = DateTime(thisDate)
        return cal.format(format)
    }

    fun getPrettySuperSimpleDateSimpleWithoutYearFromLong(thisDate: Long): String {
        return getPrettyDateSimpleWithoutYearFromLong(thisDate, null)
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

    fun convertYYYYMMddToLong(date:String):Long {
        val format = DateFormat("yyyy-MM-dd")
        val date = format.parse(date)
        return date.local.unixMillis.toLong()
    }

    fun setDate(year: Int, month: Int, dayOfMonth: Int): DateTime {
        return DateTime(year, month, dayOfMonth)

    }


    /**
     * Advance a calendar to the next occurence of a particular day (e.g. Monday, Tuesday, etc).
     *
     * @param calendar the calendar to use as the start time
     * @param dayOfWeek the day of the week to go to as per Calendar constants
     * @param incToday if true, then if the start date matches the end date, make no changes. If false,
     * and the input calendar is already on the same day of the week, then return 7
     *
     * @return A new calendar instance advanced to the next occurence of the given day of the week
     */
    fun copyCalendarAndAdvanceTo(dateTime: Long , dayOfWeek: Int, incToday: Boolean): Long {

        //Note: calendar is the calendar in the phone's time zone. The phone's timezone can be
        // different from the Class's timezone. Since all times are in the Class's time zone,
        // a phone 9 am is in fact intended to be Class TimeZone's 9 am.
        //
        // The return Calendar is the calendar where the next occurence should be. This should
        // match with the right day of the week. Hence this has to be in the Local time zone.
        // (ie: to avoid situations where next occurence clazz timezone = previous day device.
        // Since theis method is called every midnight of the phone device, we need the time to be
        // the right day (ie phone device's timezone). For this purpose we will advance to the phone
        // timezone and can set its timezone to Clazz outside this method.

        val calendar = DateTime(dateTime)
        var comparisonCalendarLong = calendar.unixMillisLong
        var comparisonCalendar = DateTime(comparisonCalendarLong)
        //TODO: Set timezone as well.

        val today = getDayOfWeek(dateTime)

        if (today == dayOfWeek){
            if (!incToday){
                val newLong = dateTime + 7 * 1000 * 60 * 60 * 24
                comparisonCalendar = DateTime(newLong)
            }

            //Addition:
            // Calendar without Time Zone's day = time zoned calendar's day = expected day of week
            if ( comparisonCalendar.dayOfWeekInt == calendar.dayOfWeekInt &&
                    calendar.dayOfWeekInt == dayOfWeek){
                return comparisonCalendar.unixMillisLong
            }

            //shit
            comparisonCalendarLong = setDayOfWeek(comparisonCalendar.unixMillisLong, dayOfWeek)
            return comparisonCalendarLong


        }


        val deltaDays: Int
        if (dayOfWeek > today) {
            deltaDays = dayOfWeek - today
        } else {
            deltaDays = 7 - today + dayOfWeek
        }

        comparisonCalendarLong = calendar.unixMillisLong + deltaDays * 1000 * 60 * 60 * 24

        return comparisonCalendarLong
    }

}
