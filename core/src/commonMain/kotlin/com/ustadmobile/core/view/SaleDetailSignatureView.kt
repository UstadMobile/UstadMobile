package com.ustadmobile.core.view

import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.db.entities.Sale


/**
 * Core View. Screen is for SaleDetailSignatureView's View
 */
interface SaleDetailSignatureView : UstadView {

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    fun updateSaleOnView(sale: Sale)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SaleDetailSignatureView"
    }


}

