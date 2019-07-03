package com.ustadmobile.core.view

interface XapiReportOptionsView : UstadView {

    fun fillVisualChartType(translatedGraphList: List<String>)

    fun fillYAxisData(translatedYAxisList: List<String>)

    fun fillXAxisAndSubGroupData(translatedXAxisList: List<String>)

    fun fillDidData(didList: List<String>)

    fun fillWhoData(whoList: List<String>)

}