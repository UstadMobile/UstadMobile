package com.ustadmobile.core.util.ext

import android.view.View
import com.google.gson.Gson
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance

/**
 * Shorthand to get the UstadMobileSystemImpl using DI from the context associated with the view
 */
val View.systemImpl: UstadMobileSystemImpl
    get() {
        val di: DI by closestDI()
        return di.direct.instance()
    }

val View.gson: Gson
    get() {
        val di: DI by closestDI()
        return di.direct.instance()
    }
