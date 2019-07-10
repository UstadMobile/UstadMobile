package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 65)
@Entity
class SalePayment() {

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

    @UmSyncMasterChangeSeqNum
    var salePaymentMCSN: Long = 0

    @UmSyncLocalChangeSeqNum
    var salePaymentLCSN: Long = 0

    @UmSyncLastChangedBy
    var salePaymentLCB: Int = 0


}
