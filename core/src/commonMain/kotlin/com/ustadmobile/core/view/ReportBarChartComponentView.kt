package com.ustadmobile.core.view

/**
 * Component for presenter access
 */
interface ReportBarChartComponentView : UstadView {

    //Any argument keys:

    fun setChartData(dataSet: List<Any>)

    companion object {

        // This defines the view name that is an argument value in the go() in impl.
        val VIEW_NAME = "ReportBarChartComponentView"
    }

}

