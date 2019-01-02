package com.ustadmobile.core.util;

import com.ustadmobile.lib.util.UMUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

/**
 * Basic calendar related utility methods. These are isolated in their own class as Calendar is not
 * supported on the GWT client.
 */
public class UMCalendarUtil {

    private static final String[] HTTP_MONTH_NAMES = new String[]{"Jan", "Feb",
            "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private static final int[] HTTP_DAYS = new int[]{ Calendar.MONDAY, Calendar.TUESDAY,
            Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY,
            Calendar.SUNDAY
    };

    private static final String[] HTTP_DAY_LABELS = new String[]{"Mon", "Tue",
            "Wed", "Thu", "Fri", "Sat", "Sun"};


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
    public static String makeHTTPDate(long time) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(new Date(time));
        StringBuffer sb = new StringBuffer();

        int val = cal.get(Calendar.DAY_OF_WEEK);
        for(int i = 0; i < HTTP_MONTH_NAMES.length; i++) {
            if(val == HTTP_DAYS[i]) {
                sb.append(HTTP_DAY_LABELS[i]).append(", ");
                break;
            }
        }
        appendTwoDigits(cal.get(Calendar.DAY_OF_MONTH), sb).append(' ');

        sb.append(HTTP_MONTH_NAMES[cal.get(Calendar.MONTH)]).append(' ');
        sb.append(checkYear(cal.get(Calendar.YEAR))).append(' ');
        appendTwoDigits(cal.get(Calendar.HOUR_OF_DAY), sb).append(':');
        appendTwoDigits(cal.get(Calendar.MINUTE), sb).append(':');
        appendTwoDigits(cal.get(Calendar.SECOND), sb).append(" GMT");

        return sb.toString();
    }

    /**
     * Parse the given http date according to :
     *  http://tools.ietf.org/html/rfc2616#section-3.3
     *
     * @param httpDate  The string http date
     * @return  The date in long
     */
    public static long parseHTTPDate(String httpDate) {
        char[] delimChars = new char[]{' ', ':', '-'};

        Vector tokens = UMUtil.tokenize(httpDate, delimChars, 0, httpDate.length());
        Calendar cal;

        if(tokens.size() == 8) {//this includes the timezone
            cal = Calendar.getInstance(TimeZone.getTimeZone(
                    (String)tokens.elementAt(7)));
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(
                    (String)tokens.elementAt(1)));
            cal.set(Calendar.MONTH, UMUtil.getIndexInArrayIgnoreCase(
                    (String)tokens.elementAt(2), HTTP_MONTH_NAMES));
            cal.set(Calendar.YEAR, checkYear(Integer.parseInt(
                    (String)tokens.elementAt(3))));

            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(
                    (String)tokens.elementAt(4)));
            cal.set(Calendar.MINUTE, Integer.parseInt(
                    (String)tokens.elementAt(5)));
            cal.set(Calendar.SECOND, Integer.parseInt(
                    (String)tokens.elementAt(6)));
        }else if(tokens.size() == 7) {
            cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.set(Calendar.MONTH, UMUtil.getIndexInArrayIgnoreCase(
                    (String)tokens.elementAt(1), HTTP_MONTH_NAMES));
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(
                    (String)tokens.elementAt(2)));
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(
                    (String)tokens.elementAt(3)));
            cal.set(Calendar.MINUTE, Integer.parseInt(
                    (String)tokens.elementAt(4)));
            cal.set(Calendar.SECOND, Integer.parseInt(
                    (String)tokens.elementAt(5)));

            cal.set(Calendar.YEAR, checkYear(Integer.parseInt(
                    (String)tokens.elementAt(6))));
        }else {
            return 0L;
        }

        return cal.getTime().getTime();
    }


    /**
     * Appends two digits for the integer i; if i < 10; prepend a leading 0
     *
     * @param i Numbe to append
     * @param sb StringBuffer to append it two
     * @return The stringbuffer
     */
    private static StringBuffer appendTwoDigits(int i, StringBuffer sb) {
        if(i < 10) {
            sb.append('0');
        }
        sb.append(i);

        return sb;
    }

    private static int checkYear(int year) {
        if(year < 30) {
            return 2000 + year;
        }else if(year < 100) {
            return 1900 + year;
        }else {
            return year;
        }
    }

    /**
     * Gets date in milli from specified pretty dateString
     * @param dateString in format dd-MM-yyyy is 23-01-1989
     * @return the long date representation
     */
    public static long getLongDateFromPrettyString(String dateString, Locale locale){
        SimpleDateFormat formatter;
        if(locale != null){
            formatter = new SimpleDateFormat("dd-MMM-yyyy", locale);
        }else{
            formatter = new SimpleDateFormat("dd-MMM-yyyy");
        }
        Date date;
        try {
            date = formatter.parse(dateString);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;

    }

    public static long getLongDateFromPrettyString(String dateString){
        return getLongDateFromPrettyString(dateString, null);
    }


    /**
     *
     * @param dateString
     * @param format
     * @param locale
     * @return
     */
    public static long getLongDateFromStringAndFormat(String dateString,
                                                      String format,
                                                      Locale locale){
        SimpleDateFormat formatter;
        if(locale != null) {
             formatter = new SimpleDateFormat(format, locale);
        }else{
            formatter = new SimpleDateFormat(format);
        }

        Date date;
        try {
            date = formatter.parse(dateString);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;

    }

    public static long getLongDateFromStringAndFormat(String dateString, String format){
        return getLongDateFromStringAndFormat(dateString, format, null);
    }



    /**
     * Gets date in long plus/minus the days specified from today.
     *
     * @param days  The days (positive or negative) off from today
     * @return  The date in long
     */
    public static long getDateInMilliPlusDays(int days){
        // get a calendar instance, which defaults to "now"
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, days);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Get date in long w.r.t plus/minus the days specified from a specified date
     * @param dateLong  The specified date (in long) where the days to be calculated.
     * @param days  The days (positive or negative) off from the dateLong specified
     * @return  The date in long
     */
    public static long getDateInMilliPlusDaysRelativeTo(long dateLong, int days){
        // get a calendar instance, which defaults to "now"
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateLong);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();

    }


    /**
     * Gets pretty looking date (eg: Mon, 23/Jan/1989) from a long date specified.
     *
     * @param thisDate The date in long for which we want a pretty date
     * @return  The pretty date for the long date specified as string.
     */
    public static String getPrettyDateFromLong(long thisDate, Locale locale){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(thisDate);
        SimpleDateFormat format;
        if(locale != null) {
            format = new SimpleDateFormat("EEEE, dd/MMMM/yyyy", locale);
        }else {
            format = new SimpleDateFormat("EEEE, dd/MMMM/yyyy");
        }
        return format.format(calendar.getTime());
    }

    /**
     * Gets pretty looking date (eg: Mon, 23/Jan/1989) from a long date specified.
     *
     * @param thisDate The date in long for which we want a pretty date
     * @return  The pretty date for the long date specified as string.
     */
    public static String getPrettyDateFromLong(long thisDate){
        return getPrettyDateFromLong(thisDate, null);
    }


    /**
     * Gets simple pretty looking date (eg; 23/Jan/89) from a long date specified.
     *
     * @param thisDate  The date in long for which we want a pretty simple date.
     * @return  The pretty simple date for the long date specified as string.
     */
    public static String getPrettyDateSuperSimpleFromLong(long thisDate, Locale locale){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(thisDate);
        SimpleDateFormat format;
        if(locale != null) {
            format = new SimpleDateFormat("dd/MMM/yy", locale);
        }else{
            format = new SimpleDateFormat("dd/MMM/yy");
        }
        return format.format(calendar.getTime());
    }

    public static String getPrettyDateSuperSimpleFromLong(long thisDate){
        return getPrettyDateSuperSimpleFromLong(thisDate, null);
    }


    /**
     * Gets simple day only (eg: Mon) from a long date specified.
     *
     * @param thisDate  The date in long for which we want the day for.
     * @return  The day for the long date specified as string.
     */
    public static String getSimpleDayFromLongDate(long thisDate, Locale locale){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(thisDate);
        SimpleDateFormat formatShortDay;
        if(locale != null) {
            formatShortDay = new SimpleDateFormat("EEE", locale);
        }else{
            formatShortDay = new SimpleDateFormat("EEE");
        }
        return formatShortDay.format(calendar.getTime());
    }

    public static String getSimpleDayFromLongDate(long thisDate){
        return getSimpleDayFromLongDate(thisDate, null);
    }


    /**
     * Checks if a given long date is today or not.
     *
     * @param date  The Date object which we want to check if its a today date.
     *
     * @return  true if given date is today, false if it isn't
     */
    public static boolean isToday(Date date){
        Calendar today = Calendar.getInstance();
        Calendar specifiedDate  = Calendar.getInstance();
        specifiedDate.setTime(date);

        return today.get(Calendar.DAY_OF_MONTH) == specifiedDate.get(Calendar.DAY_OF_MONTH)
                &&  today.get(Calendar.MONTH) == specifiedDate.get(Calendar.MONTH)
                &&  today.get(Calendar.YEAR) == specifiedDate.get(Calendar.YEAR);
    }

}
