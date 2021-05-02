package com.ustadmobile.test.port.android.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.kodein.di.DI
import org.kodein.di.DIAware

/**
 * Simple shorthand to get the KodeIN DI from the ApplicationProvider
 */
fun getApplicationDi(): DI =
        (ApplicationProvider.getApplicationContext<Context>().applicationContext as DIAware).di