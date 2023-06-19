package com.ustadmobile.core.impl

import com.ustadmobile.core.util.CountryProvider
import com.ustadmobile.core.viewmodel.CountryData
import javax.naming.Context


actual open class CountryProviderImp(
    private val applicationContext: Context
) : CountryProvider{


    actual override fun getCountry(): CountryData {
        TODO("Not yet implemented")
    }

}