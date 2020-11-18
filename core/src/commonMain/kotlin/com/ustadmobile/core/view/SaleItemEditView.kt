package com.ustadmobile.core.view

import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.SaleItem
import com.ustadmobile.lib.db.entities.SaleItemWithProduct


interface SaleItemEditView: UstadEditView<SaleItemWithProduct> {

    companion object {

        const val VIEW_NAME = "SaleItemEditEditView"

    }

}