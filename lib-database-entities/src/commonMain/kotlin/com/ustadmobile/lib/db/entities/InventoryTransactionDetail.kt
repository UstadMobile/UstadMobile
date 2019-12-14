package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable


/**
 * Person 's POJO for representing a Sale Product's transaction
 */
@Serializable
class InventoryTransactionDetail()  {

    var stockCount = 0
    var weNames : String = ""
    var saleUid : Long = 0
    var toLeUid : Long = 0
    var transactionDate: Long = 0

}
