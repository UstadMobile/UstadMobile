package com.ustadmobile.core.view

/**
 * For presenter's access to Custom views (that implement this)
 */
interface ReportTableListComponentView : CommonReportView {

    //Any argument keys:

    fun setSalesLogData(dataSet: List<Any>)

    fun setTopLEsData(dataSet: List<Any>)

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "ReportTableListComponentView"
    }
}

