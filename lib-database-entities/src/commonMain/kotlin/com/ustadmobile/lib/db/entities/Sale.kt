package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 61)
@Entity
open class Sale {
    @PrimaryKey(autoGenerate = true)
    var saleUid: Long = 0

    var saleTitle: String? = null

    var isSaleActive: Boolean = false

    var saleLocationUid: Long = 0

    var saleCreationDate: Long = 0

    var saleDueDate: Long = 0

    var saleLastUpdateDate: Long = 0

    //Person who created this sale (the salesman usually)
    var salePersonUid: Long = 0

    //any notes
    var saleNotes: String? = null

    //If created successfully - does NOT indicate payed / completed/ etc
    //If false- effectively deleted and will not show up in reports/sales list
    var isSaleDone: Boolean = false

    //Deliberate cancelled option will mark this flag as true.
    var isSaleCancelled: Boolean = false

    var isSalePreOrder: Boolean = false

    var isSalePaymentDone: Boolean = false

    var saleDiscount: Long = 0

    var saleSignature: String? = null

    @UmSyncMasterChangeSeqNum
    var saleMCSN: Long = 0

    @UmSyncLocalChangeSeqNum
    var saleLCSN: Long = 0

    @UmSyncLastChangedBy
    var saleLCB: Int = 0

    init {
        this.isSaleCancelled = false
        this.isSaleActive = true // false is essentially deleted.
        this.saleCreationDate = 0
        this.saleLastUpdateDate = this.saleCreationDate
        this.isSaleDone = false // It gets done only when Sale is delivered.
        this.isSalePreOrder = true //default to true
        this.isSalePaymentDone = true //Defaulting to true. Unless marked as done via payment addition.
        this.saleTitle = ""
        //Ideally salePaymentDone should be triggered from SaleItem and SalePayment
    }
}
