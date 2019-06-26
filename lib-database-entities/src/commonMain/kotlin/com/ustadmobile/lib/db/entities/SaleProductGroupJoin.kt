package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 71)
@Entity
class SaleProductGroupJoin {

    @PrimaryKey(autoGenerate = true)
    var saleProductGroupJoinUid: Long = 0

    var saleProductGroupJoinProductUid: Long = 0

    var saleProductGroupJoinGroupUid: Long = 0

    var isSaleProductGroupJoinActive: Boolean = false

    var saleProductGroupJoinDateCreated: Long = 0

    @UmSyncMasterChangeSeqNum
    var saleProductGroupJoinMCSN: Long = 0

    @UmSyncLocalChangeSeqNum
    var saleProductGroupJoinLCSN: Long = 0

    @UmSyncLastChangedBy
    var saleProductGroupJoinLCB: Int = 0

    constructor() {
        this.isSaleProductGroupJoinActive = false
        this.saleProductGroupJoinDateCreated = 0
    }

    constructor(productUid: Long, groupUid: Long) {
        this.saleProductGroupJoinProductUid = productUid
        this.saleProductGroupJoinGroupUid = groupUid
        this.isSaleProductGroupJoinActive = true
        this.saleProductGroupJoinDateCreated = 0
    }
}
