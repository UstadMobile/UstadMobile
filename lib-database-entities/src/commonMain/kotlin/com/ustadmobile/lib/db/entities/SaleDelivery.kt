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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SaleDelivery

        if (saleDeliveryUid != other.saleDeliveryUid) return false
        if (saleDeliverySaleUid != other.saleDeliverySaleUid) return false
        if (saleDeliverySignature != other.saleDeliverySignature) return false
        if (saleDeliveryPersonUid != other.saleDeliveryPersonUid) return false
        if (saleDeliveryDate != other.saleDeliveryDate) return false
        if (saleDeliveryActive != other.saleDeliveryActive) return false
        if (saleDeliveryMCSN != other.saleDeliveryMCSN) return false
        if (saleDeliveryLCSN != other.saleDeliveryLCSN) return false
        if (saleDeliveryLCB != other.saleDeliveryLCB) return false

        return true
    }

    override fun hashCode(): Int {
        var result = saleDeliveryUid.hashCode()
        result = 31 * result + saleDeliverySaleUid.hashCode()
        result = 31 * result + saleDeliverySignature.hashCode()
        result = 31 * result + saleDeliveryPersonUid.hashCode()
        result = 31 * result + saleDeliveryDate.hashCode()
        result = 31 * result + saleDeliveryActive.hashCode()
        result = 31 * result + saleDeliveryMCSN.hashCode()
        result = 31 * result + saleDeliveryLCSN.hashCode()
        result = 31 * result + saleDeliveryLCB
        return result
    }


}