package com.ustadmobile.core.impl

import android.content.Context
import com.ustadmobile.core.util.CountryProvider
import com.ustadmobile.core.viewmodel.CountryData
import android.telephony.TelephonyManager
import android.util.Log

actual open class CountryProviderImp(
    private val applicationContext: Context
) : CountryProvider {

    actual override fun getCountry(): CountryData {
        val tm = applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?

        return CountryData(
            countryIsoCode = tm?.networkCountryIso ?: ""
        )
    }


}