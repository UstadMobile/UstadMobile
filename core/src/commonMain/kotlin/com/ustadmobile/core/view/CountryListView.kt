package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Country

interface CountryListView : UstadView {

    fun finishWithResult(country: Country)

    companion object {

        const val VIEW_NAME = "CountryList"
    }

}