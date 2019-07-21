package com.ustadmobile.port.android.impl

import java.util.concurrent.atomic.AtomicLong

//A singleton
class LastActive private constructor() {

    //Atomic is thread safe. Can read and write at the same time.
    var lastActive: AtomicLong? = null

    companion object {
        val instance = LastActive()
    }
}
