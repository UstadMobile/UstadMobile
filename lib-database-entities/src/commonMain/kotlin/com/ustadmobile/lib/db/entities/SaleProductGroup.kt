package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable


@SyncableEntity(tableId = 69)
@Entity
@Serializable
class SaleProductGroup() {

    @PrimaryKey(autoGenerate = true)
    var saleProductGroupUid: Long = 0

    //Name of collection. eg: Eid Collection or Toys
    var saleProductGroupName: String? = null

    //Desc eg: This if for eid holidays
    var saleProductGroupDesc: String? = null

    //If collection is to be displayed from one start date
    var saleProductGroupStartDate: Long = 0

    //If collection is to be displayed from one start date to one end date
    var saleProductGroupEndDate: Long = 0

    //Creation of this collection
    var saleProductGroupCreationDate: Long = 0

    var saleProductGroupActive: Boolean = false

    var saleProductGroupType: Int = 0

    @MasterChangeSeqNum
    var saleProductGroupMCSN: Long = 0

    @LocalChangeSeqNum
    var saleProductGroupLCSN: Long = 0

    @LastChangedBy
    var saleProductGroupLCB: Int = 0


    init {
        this.saleProductGroupActive = true
        this.saleProductGroupCreationDate = 0
        this.saleProductGroupType = PRODUCT_GROUP_TYPE_CATEGORY
        this.saleProductGroupDesc = ""
    }

    constructor(name: String) : this() {
        this.saleProductGroupActive = true
        this.saleProductGroupCreationDate = 0
        this.saleProductGroupType = PRODUCT_GROUP_TYPE_CATEGORY
        this.saleProductGroupName = name
        this.saleProductGroupDesc = ""
    }

    companion object {

        const val PRODUCT_GROUP_TYPE_CATEGORY = 1
        const val PRODUCT_GROUP_TYPE_COLLECTION = 2
        const val PRODUCT_GROUP_TYPE_PRODUCT = 4
    }
}
