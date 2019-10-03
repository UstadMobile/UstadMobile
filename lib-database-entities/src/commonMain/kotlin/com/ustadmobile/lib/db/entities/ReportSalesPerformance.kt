package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ReportSalesPerformance {

    var saleAmount: Long = 0
    var locationName: String ?= null
    var locationUid: Long = 0
    var saleUid: Long = 0
    var saleCreationDate: String ?= null
    var dateGroup: String ?= null
    var firstDateOccurence: String ?= null
    var saleProductName: String ?= null
    var saleItemQuantity: Int = 0
    var producerName: String ?= null
    var producerUid: Long = 0
    var leName: String ?= null
    var leUid: Long = 0
    var productTypeName: String ?= null
    var productTypeUid: Long = 0
    var grantee: String ?= null
}
