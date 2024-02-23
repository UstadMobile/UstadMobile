package com.ustadmobile.core.domain.share

import android.content.Context
import android.content.Intent

class ShareTextUseCaseAndroid(
    private val appContext: Context
) : ShareTextUseCase{

    override fun invoke(text: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }

        val shareIntent = Intent.createChooser(intent, null).apply {
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        appContext.startActivity(shareIntent)
    }

}