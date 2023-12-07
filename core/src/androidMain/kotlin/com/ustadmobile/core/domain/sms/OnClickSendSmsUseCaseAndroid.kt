package com.ustadmobile.core.domain.sms

import android.content.Context
import android.content.Intent
import android.net.Uri

class OnClickSendSmsUseCaseAndroid(
    private val appContext: Context
) : OnClickSendSmsUseCase {

    override fun onClickSendSms(number: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setData(Uri.parse("smsto:$number"))
        appContext.startActivity(intent)
    }

}