package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 69)
@Entity
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

    @UmSyncMasterChangeSeqNum
    var saleProductGroupMCSN: Long = 0

    @UmSyncLocalChangeSeqNum
    var saleProductGroupLCSN: Long = 0

    @UmSyncLastChangedBy
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
