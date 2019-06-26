package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.*

@UmEntity(tableId = 63)
@Entity
open class SaleItem {

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
    var isSaleItemSold: Boolean = false

    //If pre order ticked.
    var isSaleItemPreorder: Boolean = false

    //Any specific discount applied (not used at the moment)
    var saleItemDiscount: Float = 0.toFloat()

    //If active or not (false is effectively deleted
    // and is usually set for sale items not saved during creation)
    var isSaleItemActive: Boolean = false

    //Date when the sale item was created (Usually current system time)
    var saleItemCreationDate: Long = 0

    //Due date of the sale item. Will only be used if saleItemPreorder is true.
    var saleItemDueDate: Long = 0

    //Stores the actual signature data
    var saleItemSignature: String? = null

    @UmSyncMasterChangeSeqNum
    var saleItemMCSN: Long = 0

    @UmSyncLocalChangeSeqNum
    var saleItemLCSN: Long = 0

    @UmSyncLastChangedBy
    var saleItemLCB: Int = 0

    constructor() {
        this.saleItemCreationDate = 0
        this.isSaleItemActive = false
        this.isSaleItemSold = false
        this.isSaleItemPreorder = false
    }

    constructor(productUid: Long) {
        this.saleItemCreationDate = 0
        this.isSaleItemActive = false
        this.isSaleItemSold = true
        this.isSaleItemPreorder = false
        this.saleItemProductUid = productUid
        this.saleItemPricePerPiece = 0F
        this.saleItemQuantity = 0
    }

    constructor(productUid: Long, quantity: Int, ppp: Long, saleUid: Long, dueDate: Long) {
        this.saleItemCurrency = "Afs"
        this.isSaleItemActive = true
        this.saleItemCreationDate = 0
        this.saleItemProductUid = productUid
        this.saleItemQuantity = quantity
        this.saleItemPricePerPiece = ppp.toFloat()
        this.saleItemSaleUid = saleUid
        this.saleItemDueDate = dueDate
        this.isSaleItemSold = true

    }
}
