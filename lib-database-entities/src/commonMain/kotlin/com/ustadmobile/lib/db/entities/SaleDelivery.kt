package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.SaleDelivery.Companion.CATEGORY_TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = CATEGORY_TABLE_ID)
@Serializable
open class SaleDelivery() {

    @PrimaryKey(autoGenerate = true)
    var saleDeliveryUid: Long = 0

    var saleDeliverySaleUid : Long = 0

    var saleDeliverySignature : String = ""

    var saleDeliveryPersonUid : Long = 0

    var saleDeliveryDate : Long = 0

    var saleDeliveryActive: Boolean = true

    @MasterChangeSeqNum
    var saleDeliveryMCSN: Long = 0

    @LocalChangeSeqNum
    var saleDeliveryLCSN: Long = 0

    @LastChangedBy
    var saleDeliveryLCB: Int = 0


    companion object{
        const val CATEGORY_TABLE_ID = 315
    }


}