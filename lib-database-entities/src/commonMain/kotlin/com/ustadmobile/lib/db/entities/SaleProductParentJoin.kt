package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.SaleProductParentJoin.Companion.SALE_PRODUCT_PARENT_JOIN_TABlE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = SALE_PRODUCT_PARENT_JOIN_TABlE_ID)
@Serializable
open class SaleProductParentJoin() {

    @PrimaryKey(autoGenerate = true)
    var saleProductParentJoinUid: Long = 0

    //Parent product or category eg: Science Books
    var saleProductParentJoinParentUid: Long = 0

    //Child product eg: A brief history of time
    var saleProductParentJoinChildUid: Long = 0

    var saleProductParentJoinActive: Boolean = false

    var saleProductParentJoinDateCreated: Long = 0

    @MasterChangeSeqNum
    var saleProductParentJoinMCSN: Long = 0

    @LocalChangeSeqNum
    var saleProductParentJoinLCSN: Long = 0

    @LastChangedBy
    var saleProductParentJoinLCB: Int = 0

    constructor(childUid: Long, parentUid: Long) : this() {
        this.saleProductParentJoinParentUid = parentUid
        this.saleProductParentJoinChildUid = childUid
        this.saleProductParentJoinActive = false
        this.saleProductParentJoinDateCreated = 0
    }

    constructor(childUid: Long, parentUid: Long, activate: Boolean) : this() {
        this.saleProductParentJoinParentUid = parentUid
        this.saleProductParentJoinChildUid = childUid
        this.saleProductParentJoinDateCreated = 0
        this.saleProductParentJoinActive = activate
    }


    companion object{
        const val SALE_PRODUCT_PARENT_JOIN_TABlE_ID = 207
    }


}