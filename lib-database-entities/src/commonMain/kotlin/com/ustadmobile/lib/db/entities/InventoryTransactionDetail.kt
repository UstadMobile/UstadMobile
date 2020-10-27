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
    var leName : String = ""
    var fromLeUid: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InventoryTransactionDetail

        if (stockCount != other.stockCount) return false
        if (weNames != other.weNames) return false
        if (saleUid != other.saleUid) return false
        if (toLeUid != other.toLeUid) return false
        if (leName != other.leName) return false
        if (transactionDate != other.transactionDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = stockCount
        result = 31 * result + weNames.hashCode()
        result = 31 * result + saleUid.hashCode()
        result = 31 * result + toLeUid.hashCode()
        result = 31 * result + transactionDate.hashCode()
        return result
    }


}
