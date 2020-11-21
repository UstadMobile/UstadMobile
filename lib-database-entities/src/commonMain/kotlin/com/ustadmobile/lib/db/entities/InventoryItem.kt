package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.InventoryItem.Companion.INVENTORY_ITEM_TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = INVENTORY_ITEM_TABLE_ID)
@Serializable
open class InventoryItem() {

    @PrimaryKey(autoGenerate = true)
    var inventoryItemUid: Long = 0

    //The Product this inventory represents
    var inventoryItemProductUid : Long = 0

    //The LE adding this
    var inventoryItemLeUid : Long = 0

    //The WE producer making this
    var inventoryItemWeUid : Long = 0

    var inventoryItemDateAdded: Long = 0

    var inventoryItemDayAdded : Long = 0

    var inventoryItemActive: Boolean = true

    @MasterChangeSeqNum
    var inventoryItemMCSN: Long = 0

    @LocalChangeSeqNum
    var inventoryItemLCSN: Long = 0

    @LastChangedBy
    var inventoryItemLCB: Int = 0


    companion object{
        const val INVENTORY_ITEM_TABLE_ID = 205
    }


}