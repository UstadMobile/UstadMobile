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

    fun setDateRangeSelectedLongs(fromDate: Long, toDate: Long)

    fun setSalePriceRangeSelected(from: Int, to: Int, salePriceSelected: String)

    fun setGroupByPresets(presets: Array<String?>, setGroupByPresets: Int)

    fun setEditMode(editMode: Boolean)

    fun showLEsOption(show: Boolean)

    companion object {

        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "ReportOptionsDetail"

        //Any argument keys:
        public val ARG_DASHBOARD_ENTRY_UID = "ArgDashboardEntryUid"
        public val ARG_REPORT_TYPE = "ArgReportType"
        public val ARG_REPORT_OPTIONS = "ArgReportOptions"
    }

}

