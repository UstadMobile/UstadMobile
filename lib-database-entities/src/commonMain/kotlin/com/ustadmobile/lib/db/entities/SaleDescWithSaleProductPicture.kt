package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class SaleDescWithSaleProductPicture() : SaleProductPicture(){

    var name: String? = null
    var description: String? = null
    var type: Int = 0
    var productUid: Long = 0
    var productGroupUid: Long = 0
}
