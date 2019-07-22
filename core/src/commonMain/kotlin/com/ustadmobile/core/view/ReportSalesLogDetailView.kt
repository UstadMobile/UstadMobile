package com.ustadmobile.core.view


/**
 * Core View. Screen is for ReportSalesLogDetail's View
 */
interface ReportSalesLogDetailView : ReportDetailView {

    //Any argument keys:

    /**
     * Method to finish the screen / view.
     */
    override fun finish()

    companion object {


        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "ReportSalesLogDetail"
    }


}

