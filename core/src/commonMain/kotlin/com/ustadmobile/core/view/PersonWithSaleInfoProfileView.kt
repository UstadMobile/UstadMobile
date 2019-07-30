package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Person


/**
 * Core View. Screen is for SaleList's View
 */
interface PersonWithSaleInfoProfileView : UstadView {

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    fun updatePersonOnView(person: Person)

    fun updateImageOnView(imagePath: String)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "PersonWithSaleInfoDetail"

    }


}

