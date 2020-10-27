package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.InventoryTransaction.Companion.INVENTORY_TRANSACTION_TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = INVENTORY_TRANSACTION_TABLE_ID)
@Serializable
open class InventoryTransaction() {

    @PrimaryKey(autoGenerate = true)
    var inventoryTransactionUid: Long = 0

    var inventoryTransactionInventoryItemUid : Long = 0

    //If inital add, leUid will be set, else it will be 0
    var inventoryTransactionFromLeUid : Long = 0

    //If sale, it will be set here
    var inventoryTransactionSaleUid : Long = 0

    var inventoryTransactionSaleItemUid: Long = 0

    //If given to another LE, it will be set here
    var inventoryTransactionToLeUid : Long = 0

    var inventoryTransactionDate : Long = 0

    var inventoryTransactionDay : Long = 0

    var inventoryTransactionActive: Boolean = true

    var inventoryTransactionSaleDeliveryUid : Long = 0

    @MasterChangeSeqNum
    var inventoryTransactionItemMCSN: Long = 0

    @LocalChangeSeqNum
    var inventoryTransactionItemLCSN: Long = 0

    @LastChangedBy
    var inventoryTransactionItemLCB: Int = 0

    companion object{
        const val INVENTORY_TRANSACTION_TABLE_ID = 206
    }


}