package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 77)
@Entity
class SaleProductParentJoin {

    /* GETTERS AND SETTER */

    @PrimaryKey(autoGenerate = true)
    var saleProductParentJoinUid: Long = 0

    //Parent product or category eg: Science Books
    var saleProductParentJoinParentUid: Long = 0

    //Child product eg: A brief history of time
    var saleProductParentJoinChildUid: Long = 0

    var isSaleProductParentJoinActive: Boolean = false

    var saleProductParentJoinDateCreated: Long = 0

    @UmSyncMasterChangeSeqNum
    var saleProductParentJoinMCSN: Long = 0

    @UmSyncLocalChangeSeqNum
    var saleProductParentJoinLCSN: Long = 0

    @UmSyncLastChangedBy
    var saleProductParentJoinLCB: Int = 0

    constructor() {}

    constructor(childUid: Long, parentUid: Long) {
        this.saleProductParentJoinParentUid = parentUid
        this.saleProductParentJoinChildUid = childUid
        this.isSaleProductParentJoinActive = false
        this.saleProductParentJoinDateCreated = 0
    }

    constructor(childUid: Long, parentUid: Long, activate: Boolean) {
        this.saleProductParentJoinParentUid = parentUid
        this.saleProductParentJoinChildUid = childUid
        this.saleProductParentJoinDateCreated = 0
        this.isSaleProductParentJoinActive = activate
    }
}
