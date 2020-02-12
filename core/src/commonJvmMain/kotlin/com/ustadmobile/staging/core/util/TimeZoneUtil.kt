package com.ustadmobile.staging.core.util

import java.util.*;
import java.text.SimpleDateFormat;

actual class TimeZoneUtil {

    actual fun getDeviceTimezone(): String {
        val tz = TimeZone.getDefault()
        val tzString = tz.getDisplayName(false, TimeZone.SHORT);

        return tzString
    }

    actual fun getPrettyDateWithTimeFromLongSimple(thisDate: Long, locale: String): String{

        val calendar = Calendar.getInstance()
        calendar.setTimeInMillis(thisDate)

        var setThisLocale = Locale.getDefault()
        if(locale.equals("ps") || locale.equals("fa") || locale.equals("ar")){
            setThisLocale = Locale("ar")
        }
        val format = SimpleDateFormat("HH:mm,  dd MMM yy", setThisLocale);

        return format.format(calendar.getTime());
    }

}