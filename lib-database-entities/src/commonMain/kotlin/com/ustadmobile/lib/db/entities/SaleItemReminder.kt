package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.SaleItemReminder.Companion.CATEGORY_TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = CATEGORY_TABLE_ID)
@Serializable
open class SaleItemReminder() {

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

    companion object{
        const val CATEGORY_TABLE_ID = 316
    }


}