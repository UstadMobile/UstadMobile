package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.Sale.Companion.CATEGORY_TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = CATEGORY_TABLE_ID)
@Serializable
open class Sale() {

    @PrimaryKey(autoGenerate = true)
    var saleUid: Long = 0

    var saleTitle: String? = null

    var saleActive: Boolean = true

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




    companion object{
        const val CATEGORY_TABLE_ID = 312
    }


}