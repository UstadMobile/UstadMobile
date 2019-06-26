package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 79)
@Entity
class SaleItemReminder {


    @PrimaryKey(autoGenerate = true)
    var saleItemReminderUid: Long = 0

    var saleItemReminderSaleItemUid: Long = 0

    var saleItemReminderDays: Int = 0

    var isSaleItemReminderActive: Boolean = false

    @UmSyncMasterChangeSeqNum
    var saleItemReminderMCSN: Long = 0

    @UmSyncLocalChangeSeqNum
    var saleItemReminderLCSN: Long = 0

    @UmSyncLastChangedBy
    var saleItemReminderLCB: Int = 0

    constructor() {
        this.isSaleItemReminderActive = false
    }

    constructor(active: Boolean) {
        this.isSaleItemReminderActive = active
    }

    constructor(days: Int, saleItemUid: Long, active: Boolean) {
        this.isSaleItemReminderActive = active
        this.saleItemReminderSaleItemUid = saleItemUid
        this.saleItemReminderDays = days
    }
}
