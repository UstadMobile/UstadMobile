package com.ustadmobile.core.domain.phonenumber

import android.content.Context
import android.content.Intent
import android.net.Uri

class OnClickPhoneNumberUseCaseAndroid(
    private val appContext: Context
): OnClickPhoneNumUseCase{

    override fun invoke(number: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setData(Uri.parse("tel:$number"))
        appContext.startActivity(intent)
    }
}