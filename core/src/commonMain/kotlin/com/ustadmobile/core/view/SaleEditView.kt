package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SaleItem
import com.ustadmobile.lib.db.entities.SaleItemWithProduct
import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.lib.db.entities.SaleWithCustomerAndLocation
import com.ustadmobile.lib.db.entities.SalePayment
import com.ustadmobile.door.DoorMutableLiveData

interface SaleEditView: UstadEditView<SaleWithCustomerAndLocation> {

    var saleItemList: DoorMutableLiveData<List<SaleItemWithProduct>>?

    var saleDeliveryList: DoorMutableLiveData<List<SaleDelivery>>?

    var salePaymentList: DoorMutableLiveData<List<SalePayment>>?

    var orderTotal: Long?

    var paymentTotal: Long?

    var balanceDue: Long?

    companion object {

        const val VIEW_NAME = "SaleEditEditView"

    }

}