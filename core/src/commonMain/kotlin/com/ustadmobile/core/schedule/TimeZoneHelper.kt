package com.ustadmobile.core.schedule

/**
 * Expect/actual function that provides the actual timezone offset at a given moment (taking the
 * ridiculous concept of daylight savings time into account)
 *
 * @param timezoneName The timezone ID e.g. Asia/Dubai
 * @param timeUtc the UTC time to get the offset for
 */
expect fun getTimezoneOffset(timezoneName: String, timeUtc: Long): Int

/**
 * Expect/actual function that will provide the fixed (e.g. raw) timezone offset that does not take
 * into account daylight savings time
 */
expect fun getRawTimezoneOffset(timezoneName: String): Int
