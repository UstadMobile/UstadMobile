package com.ustadmobile.core.view


/**
 * Core View. Screen is for ReportOptionsDetail's View
 */
interface ReportOptionsDetailView : UstadView {

    /**
     * Method to finish the screen / view.
     */
    fun finish()


    fun setTitle(title: String)

    fun setShowAverage(showAverage: Boolean)

    fun setLocationSelected(locationSelected: String)

    fun setLESelected(leSelected: String)

    fun setProductTypeSelected(productTypeSelected: String)

    fun setDateRangeSelected(dateRangeSelected: String)

    fun setSalePriceRangeSelected(from: Int, to: Int, salePriceSelected: String)

    fun setGroupByPresets(presets: Array<String>, setGroupByPresets: Int)

    fun setEditMode(editMode: Boolean)

    companion object {

        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "ReportOptionsDetail"

        //Any argument keys:
        val ARG_DASHBOARD_ENTRY_UID = "ArgDashboardEntryUid"
        val ARG_REPORT_TYPE = "ArgReportType"
        val ARG_REPORT_OPTIONS = "ArgReportOptions"
    }

}

