package com.ustadmobile.libuicompose.util.phonenum

import android.content.Context
import android.telephony.TelephonyManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ustadmobile.core.domain.phonenumber.IPhoneNumberUtil

@Composable
actual fun guessInitialPhoneCountryCode(
    phoneUtil: IPhoneNumberUtil
) : Int? {
    val context = LocalContext.current
    return remember {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE)
                    as? TelephonyManager
            val countryCode = (telephonyManager?.networkCountryIso ?: telephonyManager?.simCountryIso)?.let { phoneCountry ->
                phoneUtil.getCountryCodeForRegion(phoneCountry.uppercase())
            }
            println("Countrycode = $countryCode")
            countryCode
        }catch(e: Throwable) {
            e.printStackTrace()
            null
        }
    }
}
