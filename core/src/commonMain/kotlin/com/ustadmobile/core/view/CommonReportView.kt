package com.ustadmobile.core.view


/**
 * Core View. Screen is for ReportDetail's View
 */
interface CommonReportView : UstadView {

    //Any argument keys:
    fun downloadReport()

    companion object {

        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "ReportDetail"
    }

}

