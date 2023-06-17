package com.ustadmobile.core.impl

import android.content.Context
import com.ustadmobile.core.util.CountryProvider
import com.ustadmobile.core.viewmodel.CountryProviderUiState


actual open class CountryProviderImp(
    private val applicationContext: Context
) : CountryProvider {

    actual override fun getCountry(): CountryProviderUiState {
//        val tm = getSystemService((context as Context).TELEPHONY_SERVICE) as TelephonyManager?
//        tm.getNetworkCountryIso()
    }


}