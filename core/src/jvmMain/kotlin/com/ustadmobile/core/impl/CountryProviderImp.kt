package com.ustadmobile.core.impl

import com.ustadmobile.core.util.CountryProvider
import com.ustadmobile.core.viewmodel.CountryProviderUiState
import javax.naming.Context


actual open class CountryProviderImp(
    private val applicationContext: Context
) : CountryProvider{


    actual override fun getCountry(): CountryProviderUiState {
        TODO("Not yet implemented")
    }

}