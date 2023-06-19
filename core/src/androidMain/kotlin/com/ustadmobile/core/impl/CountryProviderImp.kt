package com.ustadmobile.core.impl

import android.content.Context
import com.ustadmobile.core.util.CountryProvider
import com.ustadmobile.core.viewmodel.CountryProviderUiState
import android.telephony.TelephonyManager
import org.kodein.di.DI
import org.kodein.di.instance

actual open class CountryProviderImp(
    private val applicationContext: Context
) : CountryProvider {

    actual override fun getCountry(): CountryProviderUiState {
        val tm = applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?

        print("message from getCountry ${tm?.networkCountryIso ?: ""}")
        return CountryProviderUiState(
            countryIsoCode = tm?.networkCountryIso ?: ""
        )
    }


}