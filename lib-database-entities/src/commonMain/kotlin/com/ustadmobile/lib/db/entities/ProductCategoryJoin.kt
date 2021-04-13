package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.ProductCategoryJoin.Companion.PRODUCT_CATEGORY_JOIN_TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = PRODUCT_CATEGORY_JOIN_TABLE_ID)
@Serializable
open class
ProductCategoryJoin() {

    @PrimaryKey(autoGenerate = true)
    var productCategoryJoinUid: Long = 0

    //Product
    var productCategoryJoinProductUid: Long = 0

    //Category
    var productCategoryJoinCategoryUid: Long = 0

    var productCategoryJoinActive: Boolean = true

    var productCategoryJoinDateCreated: Long = 0

    @MasterChangeSeqNum
    var productCategoryJoinMCSN: Long = 0

    @LocalChangeSeqNum
    var productCategoryJoinLCSN: Long = 0

    @LastChangedBy
    var productCategoryJoinLCB: Int = 0

    constructor(categoryUid: Long, productUid: Long) : this() {
        this.productCategoryJoinProductUid = productUid
        this.productCategoryJoinCategoryUid = categoryUid
        this.productCategoryJoinActive = false
        this.productCategoryJoinDateCreated = 0
    }

    constructor(categoryUid: Long, productUid: Long, activate: Boolean) : this() {
        this.productCategoryJoinProductUid = productUid
        this.productCategoryJoinCategoryUid = categoryUid
        this.productCategoryJoinDateCreated = 0
        this.productCategoryJoinActive = activate
    }


    companion object{
        const val PRODUCT_CATEGORY_JOIN_TABLE_ID = 207
    }


}