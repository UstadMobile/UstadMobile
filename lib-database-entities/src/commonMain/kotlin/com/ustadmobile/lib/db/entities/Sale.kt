package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable


@SyncableEntity(tableId = 61)
@Entity
@Serializable
open class Sale() {

    @PrimaryKey(autoGenerate = true)
    var saleUid: Long = 0

    var saleTitle: String? = null

    var saleActive: Boolean = false

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
    var saleDone: Boolean = false

    //Deliberate cancelled option will mark this flag as true.
    var saleCancelled: Boolean = false

    var salePreOrder: Boolean = false

    var salePaymentDone: Boolean = false

    var saleDiscount: Long = 0

    var saleSignature: String? = null

    var saleCustomerUid : Long = 0L

    @MasterChangeSeqNum
    var saleMCSN: Long = 0

    @LocalChangeSeqNum
    var saleLCSN: Long = 0

    @LastChangedBy
    var saleLCB: Int = 0

    constructor(active : Boolean) : this() {
        this.saleCancelled = false
        this.saleActive = active // false is essentially deleted.
        //TODO: Give it a value outside lib-database KMP
        this.saleCreationDate = 0
        this.saleLastUpdateDate = this.saleCreationDate
        this.saleDone = false // It gets done only when Sale is delivered.
        this.salePreOrder = true //default to true
        this.salePaymentDone = true //Defaulting to true. Unless marked as done via payment addition.
        this.saleTitle = ""
        this.saleLocationUid = 0
        this.saleDueDate = 0
        this.salePersonUid = 0
        this.saleNotes = ""
        this.saleDiscount = 0
        this.saleSignature = ""
        //Ideally salePaymentDone should be triggered from SaleItem and SalePayment
    }

    constructor(active: Boolean, creationDate: Long):this(active){
        this.saleCreationDate = creationDate
    }
    init {
        this.saleCancelled = false
        this.saleActive = false // false is essentially deleted.
        //TODO: Give it a value outside lib-database KMP
        this.saleCreationDate = 0
        this.saleLastUpdateDate = this.saleCreationDate
        this.saleDone = false // It gets done only when Sale is delivered.
        this.salePreOrder = true //default to true
        this.salePaymentDone = true //Defaulting to true. Unless marked as done via payment addition.
        this.saleTitle = ""
        this.saleLocationUid = 0
        this.saleDueDate = 0
        this.salePersonUid = 0
        this.saleNotes = ""
        this.saleDiscount = 0
        this.saleSignature = ""
        //Ideally salePaymentDone should be triggered from SaleItem and SalePayment
    }
}
