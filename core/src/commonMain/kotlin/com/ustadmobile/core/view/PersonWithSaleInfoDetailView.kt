package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithSaleInfo
import com.ustadmobile.lib.db.entities.SaleListDetail


/**
 * Core View. Screen is for SelectLanguageDialog's View
 */
interface PersonWithSaleInfoDetailView : UstadView {

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    fun updatePersonOnView(person: Person)

    fun setSalesFactory(factory: DataSource.Factory<Int, SaleListDetail>)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "WomenEntrepreneurDetail"

        const val ARG_WE_UID = "ArgWomenEntrepreneurPersonUid"
    }


}

