package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.InventoryTransactionDetail

interface InventoryTransactionDetailListener {

    fun onClickEntry(entry: InventoryTransactionDetail)

}