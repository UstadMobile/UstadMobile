package com.ustadmobile.core.schedule

import com.soywiz.klock.*

/**
 * Return an instance of DateTimeTz that is set to midnight (00h:00m:00s:000ms) local time
 */
inline val DateTimeTz.localMidnight: DateTimeTz get() =
    this - (hours.hours + minutes.minutes + seconds.seconds + milliseconds.milliseconds)

/**
 * Return an instance of DateTimeTz that is set to 23h:59m:59s:999ms as per local time
 */
inline val DateTimeTz.localEndOfDay: DateTimeTz get() =
    (this.localMidnight + 24.hours) - 1.milliseconds
