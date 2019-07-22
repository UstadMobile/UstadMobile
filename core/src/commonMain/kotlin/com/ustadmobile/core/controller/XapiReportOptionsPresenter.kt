package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.controller.XapiReportOptions.Companion.listOfGraphs
import com.ustadmobile.core.controller.XapiReportOptions.Companion.xAxisList
import com.ustadmobile.core.controller.XapiReportOptions.Companion.yAxisList
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.SelectMultipleEntriesTreeDialogView
import com.ustadmobile.core.view.SelectMultipleEntriesTreeDialogView.Companion.ARG_CONTENT_ENTRY_SET
import com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView
import com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView.Companion.ARG_LOCATIONS_SET
import com.ustadmobile.core.view.XapiReportDetailView
import com.ustadmobile.core.view.XapiReportOptionsView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class XapiReportOptionsPresenter(context: Any, arguments: Map<String, String>?, view: XapiReportOptionsView)
    : UstadBaseController<XapiReportOptionsView>(context, arguments!!, view) {


    private lateinit var impl: UstadMobileSystemImpl
    private lateinit var db: UmAppDatabase

    var fromDateTime: DateTime = DateTime.now()
    var fromDateTimemillis = 0L

    var toDateTime: DateTime = DateTime.now()
    var toDateTimeMillis = 0L

    private var selectedLocations: List<Long> = mutableListOf()

    private var selectedEntries: List<Long> = mutableListOf()

    private var selectedObjects: List<Long> = mutableListOf()

    private var selectedYaxis: Int = 0

    private var selectedChartType: Int = 0

    private var selectedXAxis: Int = 0

    private var selectedSubGroup: Int = 0

    private var reportOptions: XapiReportOptions? = null

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

        val json = Json(JsonConfiguration.Stable)
        val reportOptionsString = arguments[XapiReportDetailView.ARG_REPORT_OPTIONS]
        if (reportOptionsString != null) {
            reportOptions = json.parse(XapiReportOptions.serializer(), reportOptionsString)
            selectedChartType = listOfGraphs.indexOf(reportOptions!!.chartType)
            selectedYaxis = yAxisList.indexOf(reportOptions!!.yAxis)
            selectedXAxis = xAxisList.indexOf(reportOptions!!.xAxis)
            selectedSubGroup = xAxisList.indexOf(reportOptions!!.subGroup)
            selectedLocations = reportOptions!!.locationsList
            selectedEntries = reportOptions!!.entriesList
            selectedObjects = reportOptions!!.objectsList

            if (reportOptions!!.fromDate > 0L && reportOptions!!.toDate > 0L) {
                fromDateTime = DateTime(reportOptions!!.fromDate)
                toDateTime = DateTime(reportOptions!!.toDate)
                handleDateRangeSelected()
            }
            view.runOnUiThread(Runnable {
                view.updateChartTypeSelected(selectedChartType)
                view.updateYAxisTypeSelected(selectedYaxis)
                view.updateXAxisTypeSelected(selectedXAxis)
                view.updateSubgroupTypeSelected(selectedSubGroup)
            })
            GlobalScope.launch {
                if (reportOptions!!.didFilterList.isNotEmpty()) {
                    val verbs = db.xLangMapEntryDao.getAllVerbsInList(reportOptions!!.didFilterList)
                    view.runOnUiThread(Runnable {
                        view.updateDidListSelected(verbs)
                    })
                }
                if (reportOptions!!.whoFilterList.isNotEmpty()) {
                    val personList = db.personDao.getAllPersonsInList(reportOptions!!.whoFilterList)
                    view.runOnUiThread(Runnable {
                        view.updateWhoListSelected(personList)
                    })
                }
            }

        }

    }

    fun handleFromCalendarSelected(year: Int, month: Int, dayOfMonth: Int) {
        fromDateTime = UMCalendarUtil.setDate(year, month, dayOfMonth)
        handleFromCalendarSelected()
    }

    fun handleFromCalendarSelected() {
        view.runOnUiThread(Runnable { view.updateFromDialogText(fromDateTime.format("dd/MM/YYYY")) })
    }


    fun handleToCalendarSelected(year: Int, month: Int, dayOfMonth: Int) {
        toDateTime = UMCalendarUtil.setDate(year, month, dayOfMonth)
        handleToCalendarSelected()
    }

    fun handleToCalendarSelected() {
        view.runOnUiThread(Runnable { view.updateToDialogText(toDateTime.format("dd/MM/YYYY")) })
    }

    fun handleDateRangeSelected() {
        fromDateTimemillis = fromDateTime.unixMillisLong
        toDateTimeMillis = toDateTime.unixMillisLong
        view.runOnUiThread(Runnable {
            view.updateWhenRangeText(
                    fromDateTime.format("dd MMM yyyy") + " - " + toDateTime.format("dd MMM yyyy"))
        })
    }

    fun handleWhoDataTyped(name: String, uidList: List<Long>) {
        GlobalScope.launch {
            val personsNames = db.personDao.getAllPersons("%$name%", uidList)
            view.runOnUiThread(Runnable { view.updateWhoDataAdapter(personsNames) })
        }
    }

    fun handleDidDataTyped(verb: String, uidList: List<Long>) {
        GlobalScope.launch {
            val verbs = db.xLangMapEntryDao.getAllVerbs("%$verb%", uidList)
            view.runOnUiThread(Runnable { view.updateDidDataAdapter(verbs) })
        }
    }

    fun handleWhereClicked() {
        val args = mutableMapOf<String, String>()
        args[ARG_LOCATIONS_SET] = selectedLocations.joinToString { it.toString() }
        impl.go(SelectMultipleLocationTreeDialogView.VIEW_NAME, args, context)
    }


    fun handleWhatClicked() {
        val args = mutableMapOf<String, String>()
        args[ARG_CONTENT_ENTRY_SET] = selectedEntries.joinToString { it.toString() }
        impl.go(SelectMultipleEntriesTreeDialogView.VIEW_NAME, args, context)
    }

    fun handleViewReportPreview(didOptionsList: List<Long>, whoOptionsList: List<Long>) {
        reportOptions = XapiReportOptions(
                listOfGraphs[selectedChartType],
                yAxisList[selectedYaxis],
                xAxisList[selectedXAxis],
                xAxisList[selectedSubGroup],
                whoOptionsList,
                didOptionsList,
                selectedObjects,
                selectedEntries,
                fromDateTimemillis,
                toDateTimeMillis,
                selectedLocations, reportOptions?.reportTitle.toString())

        var args = HashMap<String, String?>()
        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptions!!)
        impl.go(XapiReportDetailView.VIEW_NAME, args, context)
    }

    fun handleLocationListSelected(locationList: List<Long>) {
        selectedLocations = locationList
    }

    fun handleEntriesListSelected(entriesList: List<Long>) {
        selectedEntries = entriesList
        GlobalScope.launch {
            selectedObjects = db.xObjectDao.findListOfObjectUidFromContentEntryUid(selectedEntries)
        }
    }

    fun handleSelectedYAxis(position: Int) {
        selectedYaxis = position
    }

    fun handleSelectedChartType(position: Int) {
        selectedChartType = position
    }

    fun handleSelectedXAxis(position: Int) {
        selectedXAxis = position
    }

    fun handleSelectedSubGroup(position: Int) {
        selectedSubGroup = position
    }


}