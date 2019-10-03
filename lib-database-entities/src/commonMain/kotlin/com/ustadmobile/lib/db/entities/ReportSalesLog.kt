package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ReportSalesLog {

    var leName: String? = null
    var saleValue: Long = 0
    var saleDate: Long = 0
    var productNames: String? = null
    var locationName: String? = null
}
