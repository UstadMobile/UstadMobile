package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

/**
 * Person 's POJO for representing a WE list
 */
@Serializable
class PersonWithSaleInfo() : Person() {

    var totalSale : Long = 0

    var topProducts: String? = null

    var personPictureUid: Long = 0


}