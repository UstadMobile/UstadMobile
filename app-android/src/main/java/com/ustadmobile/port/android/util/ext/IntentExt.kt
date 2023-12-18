package com.ustadmobile.port.android.util.ext

import android.content.Intent
import android.net.Uri
import com.ustadmobile.core.viewmodel.UstadViewModel
import android.util.Log

/**
 * Get the deep link that should be opened within the app from this intent (if any). See
 * UstadViewModel.ARG_OPEN_LINK
 */
fun Intent.getUstadDeepLink(): String? {
    val url = data?.toString() ?: getStringExtra(UstadViewModel.ARG_OPEN_LINK)
    return try {
        url?.also { Uri.parse(it) }
    }catch (e: Exception) {
        Log.w("UstadApp", "WARN: Invalid link could not be parsed: $url")
        null
    }
}
