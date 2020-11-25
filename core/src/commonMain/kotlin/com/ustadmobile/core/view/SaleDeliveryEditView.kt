package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.lib.db.entities.SaleItemWithProduct


interface SaleDeliveryEditView: UstadEditView<SaleDelivery> {

    //Change to SaleItemWithProductAndPersonWithInventoryCount
    var saleItems: DoorMutableLiveData<List<SaleItemWithProduct>>?

    companion object {

        const val VIEW_NAME = "SaleDeliveryEditEditView"

    }

}