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
open class Inventory() {

    @PrimaryKey(autoGenerate = true)
    var inventoryUid: Long = 0

    //The SaleProduct this inventory represents
    var inventorySaleProductUid = 0

    //Inventory items in stock
    var inventoryStock : Int = 0

    @MasterChangeSeqNum
    var inventoryMCSN: Long = 0

    @LocalChangeSeqNum
    var inventoryLCSN: Long = 0

    @LastChangedBy
    var inventoryLCB: Int = 0

    init {
    }

}
