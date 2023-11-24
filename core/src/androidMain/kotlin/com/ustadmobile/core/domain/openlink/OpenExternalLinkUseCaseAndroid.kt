package com.ustadmobile.core.domain.openlink

import android.content.Context
import android.content.Intent
import android.net.Uri

class OpenExternalLinkUseCaseAndroid(
    private val appContext: Context
): OpenExternalLinkUseCase {

    override fun invoke(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setData(Uri.parse(url))
        appContext.startActivity(intent)
    }
}