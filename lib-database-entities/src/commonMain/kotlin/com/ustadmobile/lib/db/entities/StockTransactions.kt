package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class StockTransactions() {

    var stockCount: Int = 0
    var weNames : String? = null
    var saleUid : Long = 0
    var toLeUid : Long = 0
    var transactionDate: Long = 0
    var saleTitle : String? = null
}
