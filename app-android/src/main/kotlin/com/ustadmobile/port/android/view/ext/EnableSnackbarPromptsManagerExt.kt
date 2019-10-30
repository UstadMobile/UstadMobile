package com.ustadmobile.port.android.view.ext

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.toughra.ustadmobile.R
import com.ustadmobile.sharedse.network.EnablePromptsSnackbarManager

private val STRING_ID_MAP = mapOf(
        EnablePromptsSnackbarManager.BLUETOOTH to R.string.offline_sharing_enable_bluetooth_prompt,
        EnablePromptsSnackbarManager.WIFI to R.string.offline_sharing_enable_wifi_promot)

fun EnablePromptsSnackbarManager.makeSnackbarIfRequired(rootView: View, context: Context) {
    this.makeSnackbarIfRequired(context, {messageStringId ->
        Snackbar.make(rootView, messageStringId, Snackbar.LENGTH_INDEFINITE)
    }, STRING_ID_MAP, R.string.enable)
}
