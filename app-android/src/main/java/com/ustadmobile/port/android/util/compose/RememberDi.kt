package com.ustadmobile.port.android.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.kodein.di.DI
import org.kodein.di.DIAware

@Composable
fun rememberDi(): DI? {
    val appContext = LocalContext.current.applicationContext
    return remember(appContext) {
        (appContext.applicationContext as? DIAware)?.di
    }
}


