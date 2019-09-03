package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity

@SyncableEntity(tableId = 71)
@Entity
class SaleProductGroupJoin() {

    @PrimaryKey(autoGenerate = true)
    var saleProductGroupJoinUid: Long = 0

    var saleProductGroupJoinProductUid: Long = 0

    var saleProductGroupJoinGroupUid: Long = 0

    var saleProductGroupJoinActive: Boolean = false

    var saleProductGroupJoinDateCreated: Long = 0

    @MasterChangeSeqNum
    var saleProductGroupJoinMCSN: Long = 0

    @LocalChangeSeqNum
    var saleProductGroupJoinLCSN: Long = 0

    @LastChangedBy
    var saleProductGroupJoinLCB: Int = 0

    init {
        this.saleProductGroupJoinActive = false
        this.saleProductGroupJoinDateCreated = 0
    }

    constructor(productUid: Long, groupUid: Long) : this() {
        this.saleProductGroupJoinProductUid = productUid
        this.saleProductGroupJoinGroupUid = groupUid
        this.saleProductGroupJoinActive = true
        this.saleProductGroupJoinDateCreated = 0
    }
}
