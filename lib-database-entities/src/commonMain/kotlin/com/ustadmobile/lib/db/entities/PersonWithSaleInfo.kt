package com.ustadmobile.lib.db.entities


/**
 * Person 's POJO for representing itself with Sale info (eg: Women entrepreneur list)
 */
class PersonWithSaleInfo() : Person() {

    var totalSale : Long = 0
    var topProducts: String? = null
    var personPictureUid: Long = 0

}
