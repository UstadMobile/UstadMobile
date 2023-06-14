package com.ustadmobile.core.util

import com.ustadmobile.core.viewmodel.CountryProviderUiState

interface CountryProvider {

    fun getCountry(): CountryProviderUiState

}