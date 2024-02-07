package com.ustadmobile.core.domain.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE

class SetClipboardStringUseCaseAndroid(
    appContext: Context
): SetClipboardStringUseCase {

    private val clipboardManager: ClipboardManager = appContext.getSystemService(CLIPBOARD_SERVICE)
        as ClipboardManager

    override fun invoke(content: String) {
        val clipData = ClipData.newPlainText("text", content)
        clipboardManager.setPrimaryClip(clipData)
    }

}