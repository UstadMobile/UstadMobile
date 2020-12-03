package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.SalePayment
import com.ustadmobile.lib.db.entities.SalePaymentWithSaleItems


interface SalePaymentEditView: UstadEditView<SalePaymentWithSaleItems> {

    companion object {

        const val VIEW_NAME = "SalePaymentEditEditView"

    }

}