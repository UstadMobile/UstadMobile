package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.XapiReportOptionsView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class XapiReportOptionsPresenter(context: Any, arguments: Map<String, String>?, view: XapiReportOptionsView)
    : UstadBaseController<XapiReportOptionsView>(context, arguments!!, view) {


    lateinit var impl: UstadMobileSystemImpl
    lateinit var db: UmAppDatabase

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        db = UmAccountManager.getRepositoryForActiveAccount(context)
        impl = UstadMobileSystemImpl.instance

        val translatedGraphList = listOfGraphs.map { impl.getString(it, context) }
        val translatedYAxisList = yAxisList.map { impl.getString(it, context) }
        val translatedXAxisList = xAxisList.map { impl.getString(it, context) }

        view.runOnUiThread(Runnable { view.fillVisualChartType(translatedGraphList) })

        view.runOnUiThread(Runnable { view.fillYAxisData(translatedYAxisList) })

        view.runOnUiThread(Runnable { view.fillXAxisAndSubGroupData(translatedXAxisList) })

        GlobalScope.launch {
            val objectNames = db.xLangMapEntryDao.getAllVerbs()
            view.runOnUiThread(Runnable { view.fillDidData(objectNames) })
        }


    }

    fun handleWhoDataTyped(name: String){
        GlobalScope.launch {
            val personsNames = db.personDao.getAllPersons("%$name%")
            view.runOnUiThread(Runnable { view.updateWhoDataAdapter(personsNames) })
        }
    }

    fun handleViewReportPreview(chartTypePos: Int, yAxisPos: Int, xAxisPos: Int, subGroupPos: Int, didListOptions: List<String>, whoListPos: List<Long>) {
        var report = XapiReportOptions(listOfGraphs[chartTypePos], yAxisList[yAxisPos], xAxisList[xAxisPos], xAxisList[subGroupPos])
    }

    companion object {

        private const val BAR_CHART = MessageID.bar_chart

        private const val LINE_GRAPH = MessageID.line_graph

        private const val FREQ_GRAPH = MessageID.freq_graph

        val listOfGraphs = arrayOf(BAR_CHART, LINE_GRAPH, FREQ_GRAPH)

        private const val SCORE = MessageID.score

        private const val DURATION = MessageID.duration

        private const val COUNT_ACTIVITIES = MessageID.count_activity

        val yAxisList = arrayOf(SCORE, DURATION, COUNT_ACTIVITIES)

        private const val DAY = MessageID.xapi_day

        private const val WEEK = MessageID.xapi_week

        private const val MONTH = MessageID.xapi_month

        private const val CUSTOM_DATE = MessageID.xapi_custom_date

        private const val CONTENT_ENTRY = MessageID.xapi_content_entry

        private const val LOCATION = MessageID.xapi_location

        private const val GENDER = MessageID.xapi_gender

        val xAxisList = arrayOf(DAY, WEEK, MONTH, CUSTOM_DATE, CONTENT_ENTRY, LOCATION, GENDER)

    }

    data class XapiReportOptions(var chartType: Int, var yAxis: Int,
                                 var xAxis: Int, var subGroup: Int)

}