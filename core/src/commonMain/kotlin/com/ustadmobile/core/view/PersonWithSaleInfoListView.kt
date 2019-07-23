package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithSaleInfo


/**
 * Core View. Screen is for SelectLanguageDialog's View
 */
interface PersonWithSaleInfoListView : UstadView {


    fun setWEListFactory(factory: DataSource.Factory<Int, PersonWithSaleInfo>)

    fun updateSortSpinner(presets: Array<String?>)

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    companion object {
        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "MyWomenEntrepreneurs"

        const val ARG_LE_UID = "ArgLEUidPersonUid"
    }


}

