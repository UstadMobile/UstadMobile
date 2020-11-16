package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * SaleItem 's POJO representing itself on the view (and recycler views)
 */
@Serializable
class SaleWithCustomerAndLocation() : Sale() {


    @Embedded
    var saleCustomer: Person? = null

    @Embedded
    var saleLocation: Location? = null


}