package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SaleListDetail

interface SaleListView: UstadListView<Sale, SaleListDetail> {

    companion object {
        const val VIEW_NAME = "SaleListView"
    }

}