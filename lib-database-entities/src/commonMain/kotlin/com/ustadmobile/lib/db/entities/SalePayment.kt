package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.SalePayment.Companion.CATEGORY_TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = CATEGORY_TABLE_ID)
@Serializable
open class SalePayment() {

    @PrimaryKey(autoGenerate = true)
    var salePaymentUid: Long = 0

    //The date the payment was made
    var salePaymentPaidDate: Long = 0

    //Paid amount
    var salePaymentPaidAmount: Long = 0

    //Currency of paid amount
    var salePaymentCurrency: String? = null

    //Which sale is it attached to.
    var salePaymentSaleUid: Long = 0

    //Says the payment was done. If it is false, then the amount is outstanding.
    var salePaymentDone: Boolean = true

    //If false, it wont show up on the app and reports - effectively deleted.
    var salePaymentActive: Boolean = true

    @MasterChangeSeqNum
    var salePaymentMCSN: Long = 0

    @LocalChangeSeqNum
    var salePaymentLCSN: Long = 0

    @LastChangedBy
    var salePaymentLCB: Int = 0

    companion object{
        const val CATEGORY_TABLE_ID = 317
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SalePayment

        if (salePaymentUid != other.salePaymentUid) return false
        if (salePaymentPaidDate != other.salePaymentPaidDate) return false
        if (salePaymentPaidAmount != other.salePaymentPaidAmount) return false
        if (salePaymentCurrency != other.salePaymentCurrency) return false
        if (salePaymentSaleUid != other.salePaymentSaleUid) return false
        if (salePaymentDone != other.salePaymentDone) return false
        if (salePaymentActive != other.salePaymentActive) return false
        if (salePaymentMCSN != other.salePaymentMCSN) return false
        if (salePaymentLCSN != other.salePaymentLCSN) return false
        if (salePaymentLCB != other.salePaymentLCB) return false

        return true
    }

    override fun hashCode(): Int {
        var result = salePaymentUid.hashCode()
        result = 31 * result + salePaymentPaidDate.hashCode()
        result = 31 * result + salePaymentPaidAmount.hashCode()
        result = 31 * result + (salePaymentCurrency?.hashCode() ?: 0)
        result = 31 * result + salePaymentSaleUid.hashCode()
        result = 31 * result + salePaymentDone.hashCode()
        result = 31 * result + salePaymentActive.hashCode()
        result = 31 * result + salePaymentMCSN.hashCode()
        result = 31 * result + salePaymentLCSN.hashCode()
        result = 31 * result + salePaymentLCB
        return result
    }


}