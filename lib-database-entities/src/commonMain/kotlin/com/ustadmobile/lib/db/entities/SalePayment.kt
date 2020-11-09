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
    var salePaymentDone: Boolean = false

    //If false, it wont show up on the app and reports - effectively deleted.
    var salePaymentActive: Boolean = false

    @MasterChangeSeqNum
    var salePaymentMCSN: Long = 0

    @LocalChangeSeqNum
    var salePaymentLCSN: Long = 0

    @LastChangedBy
    var salePaymentLCB: Int = 0

    companion object{
        const val CATEGORY_TABLE_ID = 317
    }


}