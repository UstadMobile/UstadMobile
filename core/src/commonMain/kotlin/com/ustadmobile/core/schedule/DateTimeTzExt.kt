package com.ustadmobile.core.schedule

import com.soywiz.klock.*

inline val DateTimeTz.localMidnight: DateTimeTz get() =
    this - (hours.hours + minutes.minutes + seconds.seconds + milliseconds.milliseconds)
