package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable


@SyncableEntity(tableId = 85)
@Entity
@Serializable
open class InventoryTransaction() {

    @PrimaryKey(autoGenerate = true)
    var inventoryTransactionUid: Long = 0

    var inventoryTransactionInventoryItemUid : Long = 0

    //If inital add, leUid will be set, else it will be 0
    var inventoryTransactionFromLeUid : Long = 0

    //If sale, it will be set here
    var inventoryTransactionSaleUid : Long = 0

    //If given to another LE, it will be set here
    var inventoryTransactionToLeUid : Long = 0

    var inventoryTransactionDate : Long = 0

    var inventoryTransactionActive: Boolean = true

    @MasterChangeSeqNum
    var inventoryTransactionItemMCSN: Long = 0

    @LocalChangeSeqNum
    var inventoryTransactionItemLCSN: Long = 0

    @LastChangedBy
    var inventoryTransactionItemLCB: Int = 0

    constructor(inventoryItemUid: Long, leUid: Long) : this(){
        inventoryTransactionInventoryItemUid = inventoryItemUid
        inventoryTransactionFromLeUid = leUid
        inventoryTransactionActive = true
    }

    constructor(inventoryItemUid: Long, leUid: Long, saleUid:Long, date: Long):this(){
        inventoryTransactionInventoryItemUid = inventoryItemUid
        inventoryTransactionFromLeUid = leUid
        inventoryTransactionActive = true
        inventoryTransactionSaleUid = saleUid
        inventoryTransactionDate = date
    }

    init {
    }

}
