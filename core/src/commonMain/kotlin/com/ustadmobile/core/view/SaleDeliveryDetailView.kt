package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.PersonWithInventory
import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.lib.db.entities.SaleItemListDetail


/**
 * Core View. Screen is for SaleList's View
 */
interface SaleDeliveryDetailView : UstadView {

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    fun setUpView(saleItems: List<SaleItemListDetail>)

    fun setUpAllViews(itemsWithProducers : HashMap<SaleItemListDetail, List<PersonWithInventory>>)

    fun updateSaleDeliveryOnView(saleDelivery: SaleDelivery)

    companion object {
        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SaleDeliveryDetail"

        const val ARG_SALE_DELIVERY_UID = "ArgSaleDeliveryUid"
        const val ARG_SALE_DELIVERY_SALE_UID = "ArgSaleDeliverySaleUid"

    }

}

