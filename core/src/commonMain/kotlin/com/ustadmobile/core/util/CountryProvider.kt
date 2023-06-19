package com.ustadmobile.core.util

import com.ustadmobile.core.viewmodel.CountryData

interface CountryProvider {

    fun getCountry(): CountryData

}