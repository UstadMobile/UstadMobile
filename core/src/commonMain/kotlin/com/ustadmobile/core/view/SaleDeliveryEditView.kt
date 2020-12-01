package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.lib.db.entities.SaleDeliveryAndItems
import com.ustadmobile.lib.db.entities.ProductDeliveryWithProductAndTransactions


interface SaleDeliveryEditView: UstadEditView<SaleDeliveryAndItems> {

    var productWithDeliveries: DoorMutableLiveData<List<ProductDeliveryWithProductAndTransactions>>?
    var productWithDeliveriesList: List<ProductDeliveryWithProductAndTransactions>

    companion object {

        const val VIEW_NAME = "SaleDeliveryEditView"

    }

}