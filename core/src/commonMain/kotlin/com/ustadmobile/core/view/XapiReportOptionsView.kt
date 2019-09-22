package com.ustadmobile.core.view

import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.XLangMapEntryDao
import kotlin.js.JsName

interface XapiReportOptionsView : UstadView, UstadViewWithProgress {

    @JsName("fillVisualChartType")
    fun fillVisualChartType(translatedGraphList: List<String>)

    @JsName("fillYAxisData")
    fun fillYAxisData(translatedYAxisList: List<String>)

    @JsName("fillXAxisAndSubGroupData")
    fun fillXAxisAndSubGroupData(translatedXAxisList: List<String>)

    @JsName("updateWhoDataAdapter")
    fun updateWhoDataAdapter(whoList: List<PersonDao.PersonNameAndUid>)

    @JsName("updateDidDataAdapter")
    fun updateDidDataAdapter(didList: List<XLangMapEntryDao.Verb>)

    @JsName("updateFromDialogText")
    fun updateFromDialogText(fromDate: String)

    @JsName("updateToDialogText")
    fun updateToDialogText(toDate: String)

    @JsName("updateWhenRangeText")
    fun updateWhenRangeText(rangeText: String)

    @JsName("updateChartTypeSelected")
    fun updateChartTypeSelected(indexChart: Int)

    @JsName("updateYAxisTypeSelected")
    fun updateYAxisTypeSelected(indexYAxis: Int)

    @JsName("updateXAxisTypeSelected")
    fun updateXAxisTypeSelected(indexXAxis: Int)

    @JsName("updateSubgroupTypeSelected")
    fun updateSubgroupTypeSelected(indexSubgroup: Int)

    @JsName("updateWhoListSelected")
    fun updateWhoListSelected(personList: List<PersonDao.PersonNameAndUid>)

    @JsName("updateDidListSelected")
    fun updateDidListSelected(verbs: List<XLangMapEntryDao.Verb>)

    companion object {

        const val VIEW_NAME = "XapiReportOptionsView"



    }

}