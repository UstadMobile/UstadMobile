package com.ustadmobile.core.impl

import android.content.Context
import com.ustadmobile.core.util.CountryProvider
import com.ustadmobile.core.viewmodel.CountryProviderUiState
import android.telephony.TelephonyManager
import org.kodein.di.DI
import org.kodein.di.instance

actual open class CountryProviderImp(
    private val applicationContext: Context,
    private val di: DI
) : CountryProvider {

    actual override fun getCountry(): CountryProviderUiState {
        val tm = applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        tm?.networkCountryIso

        val countryProvider: CountryProvider by di.instance()

        return countryProvider.getCountry()
    }


}