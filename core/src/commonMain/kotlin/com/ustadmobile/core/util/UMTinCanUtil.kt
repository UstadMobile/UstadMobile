/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.core.util

import com.soywiz.klock.ISO8601
import kotlin.math.floor

/**
 * This class holds static utility functions to use with TinCan logic.
 *
 * @author mike
 */
object UMTinCanUtil {

    const val ADL_PREFIX_VERB = "http://adlnet.gov/expapi/verbs/"

    const val VERB_PASSED = ADL_PREFIX_VERB + "passed"

    const val VERB_FAILED = ADL_PREFIX_VERB + "failed"

    const val VERB_ANSWERED = ADL_PREFIX_VERB + "answered"

    /**
     * Format an ISO 8601 Duration from the number of milliseconds
     *
     * @param duration Duration time in MS
     *
     * @return A string formatted according to ISO8601 Duration e.g. P2H1M15S
     */
    fun format8601Duration(duration: Long): String {
        val msPerHour = 1000 * 60 * 60
        val hours = floor((duration / msPerHour).toDouble()).toInt()
        var durationRemaining = duration % msPerHour

        val msPerMin = 60 * 1000
        val mins = floor((durationRemaining / msPerMin).toDouble()).toInt()
        durationRemaining %= msPerMin

        val msPerS = 1000
        val secs = floor((durationRemaining / msPerS).toDouble()).toInt()

        return "PT" + hours + "H" + mins + "M" + secs + "S"
    }

    fun parse8601Duration(duration: String): Long {
        val time = ISO8601.IsoIntervalFormat("PTnnHnnMnnS").tryParse(duration, doThrow = false)
        return time?.totalMilliseconds?.toLong() ?: 0L
    }

    fun parse8601DurationOrDefault(duration: String?, defaultDuration: Long = 0L): Long {
        return if(duration != null) {
            parse8601Duration(duration)
        }else {
            defaultDuration
        }
    }

}
