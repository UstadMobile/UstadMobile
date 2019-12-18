package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.PersonWithInventory


/**
 * Core View. Screen is for SelectProducer's View
 */
interface SelectProducersView : UstadView {

    /**
     * Method to finish the screen / view.
     */
    fun finish()

    fun updateProducersOnView(producers: List<PersonWithInventory>)

    fun updateSpinner(presents: Array<String?>)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        const val VIEW_NAME = "SelectProducers"

        //Any argument keys:

        const val ARG_SELECT_PRODUCERS_INVENTORY_MODE = "ArgSelectProducersInventoryMode"

        const val ARG_SELECT_PRODUCERS_INVENTORY_ADDITION = "ArgSelectProducersInventoryAddition"
        const val ARG_SELECT_PRODUCERS_INVENTORY_SELECTION = "ArgSelectProducersInventorySelection"


        const val ARG_SELECT_PRODUCERS_SALE_PRODUCT_UID = "saleProducersSaleProductUid"
        const val ARG_SELECT_PRODUCERS_SALE_UID = "ArgSelectProducersSaleUid"
        const val ARG_SELECT_PRODUCERS_SALE_ITEM_UID = "ArgSelectProducersSaleItemUid"
        const val ARG_SELECT_PRODUCERS_SALE_ITEM_PREORDER = "ArgSelectProducersSaleItemPreOrder"

    }


}

