package com.ustadmobile.core.domain.sendemail

import android.content.Context
import android.content.Intent
import android.net.Uri

class OnClickEmailUseCaseAndroid(
    private val appContext: Context
): OnClickEmailUseCase {

    override fun invoke(emailAddr: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.putExtra(Intent.EXTRA_EMAIL, emailAddr)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setData(Uri.parse("mailto:"))
        appContext.startActivity(intent)
    }
}