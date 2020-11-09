package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.SaleItem.Companion.CATEGORY_TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = CATEGORY_TABLE_ID)
@Serializable
open class SaleItem() {

    @PrimaryKey(autoGenerate = true)
    var saleItemUid: Long = 0

    // The Sale uid
    var saleItemSaleUid: Long = 0

    //Producer person uid
    var saleItemProducerUid: Long = 0

    //Sale product uid
    var saleItemProductUid: Long = 0

    //Quantity of sale product
    var saleItemQuantity: Int = 0

    //Price per product
    var saleItemPricePerPiece: Float = 0.toFloat()

    //Currency (eg: Afs)
    var saleItemCurrency: String? = null

    //If sold ticked.
    var saleItemSold: Boolean = false

    //If pre order ticked.
    var saleItemPreorder: Boolean = false

    //Any specific discount applied (not used at the moment)
    var saleItemDiscount: Float = 0.toFloat()

    //If active or not (false is effectively deleted
    // and is usually set for sale items not saved during creation)
    var saleItemActive: Boolean = false

    //Date when the sale item was created (Usually current system time)
    var saleItemCreationDate: Long = 0

    //Due date of the sale item. Will only be used if saleItemPreorder is true.
    var saleItemDueDate: Long = 0

    //Stores the actual signature data
    var saleItemSignature: String? = null

    @MasterChangeSeqNum
    var saleItemMCSN: Long = 0

    @LocalChangeSeqNum
    var saleItemLCSN: Long = 0

    @LastChangedBy
    var saleItemLCB: Int = 0




    companion object{
        const val CATEGORY_TABLE_ID = 313
    }


}