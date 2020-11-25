package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.SalePayment


interface SalePaymentEditView: UstadEditView<SalePayment> {

    companion object {

        const val VIEW_NAME = "SalePaymentEditEditView"

    }

}