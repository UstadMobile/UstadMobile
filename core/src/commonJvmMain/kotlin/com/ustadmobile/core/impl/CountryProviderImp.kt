package com.ustadmobile.core.impl

import com.ustadmobile.core.util.CountryProvider
import com.ustadmobile.core.viewmodel.CountryProviderUiState
import javax.naming.Context

expect class CountryProviderImp(
    context: Context
) : CountryProvider {

    override fun getCountry(): CountryProviderUiState

}