package com.ustadmobile.core.model

import com.ustadmobile.core.controller.ReportOptionsDetailPresenter.Companion.GROUP_BY_LOCATION
import com.ustadmobile.core.util.UMCalendarUtil
import kotlinx.serialization.Serializable


/**
 * Simple POJO of Report Options that will be converted to JSON and vice versa
 */
@Serializable
class ReportOptions {

    var productTypes: List<Long>? = null
    var groupBy: Int = 0
    var isShowAverage: Boolean = false
    var les: List<Long>? = null

    var locations: MutableList<Long> = mutableListOf()
    var fromDate: Long = 0
    var toDate: Long = 0
    var fromPrice: Int = 0
    var toPrice: Int = 0

    init {
        //Defaults
        productTypes = ArrayList()
        groupBy = GROUP_BY_LOCATION
        isShowAverage = true
        les = ArrayList()
        locations = mutableListOf()
        fromDate = UMCalendarUtil.getDateInMilliPlusDays(-31)
        toDate = UMCalendarUtil.getDateInMilliPlusDays(0)
        fromPrice = 0
        toPrice = 75000
    }
}
