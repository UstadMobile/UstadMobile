package com.ustadmobile.staging.core.util

import java.util.*;

actual class TimeZoneUtil {

    actual fun getDeviceTimezone(): String {
        val tz = TimeZone.getDefault()
        val tzString = tz.getDisplayName(false, TimeZone.SHORT);
        print(tz.getRawOffset())

        return tzString
    }

}