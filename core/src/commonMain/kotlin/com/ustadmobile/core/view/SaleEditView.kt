package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SaleItem
import com.ustadmobile.lib.db.entities.SaleDelivery
import com.ustadmobile.lib.db.entities.SalePayment
import com.ustadmobile.door.DoorMutableLiveData

interface SaleEditView: UstadEditView<Sale> {

    var saleItemList: DoorMutableLiveData<List<SaleItem>>?

    var saleDeliveryList: DoorMutableLiveData<List<SaleDelivery>>?

    var salePaymentList: DoorMutableLiveData<List<SalePayment>>?

    var totalAmountLive: DoorLiveData<Long>?

    var totalAmount: Long?

    var balanceDue: Long?

    companion object {

        const val VIEW_NAME = "SaleEditEditView"

    }

}