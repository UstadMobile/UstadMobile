package com.ustadmobile.core.impl

import android.content.Context
import com.ustadmobile.core.util.CountryProvider
import com.ustadmobile.core.viewmodel.CountryProviderUiState
import android.telephony.TelephonyManager

actual class CountryProviderImp(
    context: Context,
): CountryProvider {

    actual override fun getCountry(): CountryProviderUiState {
        TODO("Not yet implemented")
    }

}