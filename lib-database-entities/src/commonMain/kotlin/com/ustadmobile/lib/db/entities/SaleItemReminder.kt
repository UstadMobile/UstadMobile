package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable


@SyncableEntity(tableId = 79)
@Entity
@Serializable
class SaleItemReminder() {


    @PrimaryKey(autoGenerate = true)
    var saleItemReminderUid: Long = 0

    var saleItemReminderSaleItemUid: Long = 0

    var saleItemReminderDays: Int = 0

    var saleItemReminderActive: Boolean = false

    @MasterChangeSeqNum
    var saleItemReminderMCSN: Long = 0

    @LocalChangeSeqNum
    var saleItemReminderLCSN: Long = 0

    @LastChangedBy
    var saleItemReminderLCB: Int = 0

    init {
        this.saleItemReminderActive = false
    }

    constructor(active: Boolean) : this() {
        this.saleItemReminderActive = active
    }

    constructor(days: Int, saleItemUid: Long, active: Boolean) : this() {
        this.saleItemReminderActive = active
        this.saleItemReminderSaleItemUid = saleItemUid
        this.saleItemReminderDays = days
    }
}
