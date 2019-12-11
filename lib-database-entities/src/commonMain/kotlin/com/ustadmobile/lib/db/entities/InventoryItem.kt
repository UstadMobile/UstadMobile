package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable


@SyncableEntity(tableId = 84)
@Entity
@Serializable
open class InventoryItem() {

    @PrimaryKey(autoGenerate = true)
    var inventoryItemUid: Long = 0

    //The SaleProduct this inventory represents
    var inventoryItemSaleProductUid = 0

    //The LE adding this
    var inventoryItemLeUid : Long = 0

    //The WE producer making this
    var inventoryItemWeUid : Long = 0

    var inventoryItemDateAdded: Long = 0

    var inventoryItemActive: Boolean = true

    @MasterChangeSeqNum
    var inventoryItemMCSN: Long = 0

    @LocalChangeSeqNum
    var inventoryItemLCSN: Long = 0

    @LastChangedBy
    var inventoryItemLCB: Int = 0

    init {
    }

}
