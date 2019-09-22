package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.lib.db.entities.XapiReportOptions.Companion.listOfGraphs
import com.ustadmobile.lib.db.entities.XapiReportOptions.Companion.xAxisList
import com.ustadmobile.lib.db.entities.XapiReportOptions.Companion.yAxisList
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.XLangMapEntryDao
import com.ustadmobile.core.db.dao.XObjectDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.SelectMultipleEntriesTreeDialogView
import com.ustadmobile.core.view.SelectMultipleEntriesTreeDialogView.Companion.ARG_CONTENT_ENTRY_SET
import com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView
import com.ustadmobile.core.view.SelectMultipleLocationTreeDialogView.Companion.ARG_LOCATIONS_SET
import com.ustadmobile.core.view.XapiReportDetailView
import com.ustadmobile.core.view.XapiReportOptionsView
import com.ustadmobile.lib.db.entities.XapiReportOptions
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.js.JsName

class XapiReportOptionsPresenter(context: Any, arguments: Map<String, String>?,
                                 view: XapiReportOptionsView, private val personDao: PersonDao,
                                 private val xObjectDao: XObjectDao, private val xLangMapEntryDao: XLangMapEntryDao)
    : UstadBaseController<XapiReportOptionsView>(context, arguments!!, view) {


    private lateinit var impl: UstadMobileSystemImpl

    var fromDateTime: DateTime = DateTime.now()
    private var fromDateTimemillis = 0L

    var toDateTime: DateTime = DateTime.now()
    private var toDateTimeMillis = 0L

    private var selectedLocations: List<Long> = mutableListOf()

    private var selectedEntries: List<Long> = mutableListOf()

    private var selectedObjects: List<Long> = mutableListOf()

    private var selectedYaxis: Int = 0

    private var selectedChartType: Int = 0

    private var selectedXAxis: Int = 0

    private var selectedSubGroup: Int = 0

    private var reportOptions: XapiReportOptions? = null

    private var activeJobCount = 0

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        impl = UstadMobileSystemImpl.instance

        val translatedGraphList = listOfGraphs.map { impl.getString(it, context) }
        val translatedYAxisList = yAxisList.map { impl.getString(it, context) }
        val translatedXAxisList = xAxisList.map { impl.getString(it, context) }

        view.fillVisualChartType(translatedGraphList)

        view.fillYAxisData(translatedYAxisList)

        view.fillXAxisAndSubGroupData(translatedXAxisList)

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

            view.updateChartTypeSelected(selectedChartType)
            view.updateYAxisTypeSelected(selectedYaxis)
            view.updateXAxisTypeSelected(selectedXAxis)
            view.updateSubgroupTypeSelected(selectedSubGroup)

            activeJobCount += 1
            view.showBaseProgressBar(activeJobCount > 0)
            GlobalScope.launch {
                if (reportOptions!!.didFilterList.isNotEmpty()) {
                    val verbs = xLangMapEntryDao.getAllVerbsInList(reportOptions!!.didFilterList)
                    view.runOnUiThread(Runnable {
                        view.updateDidListSelected(verbs)
                    })
                }
                if (reportOptions!!.whoFilterList.isNotEmpty()) {
                    val personList = personDao.getAllPersonsInList(reportOptions!!.whoFilterList)
                    view.runOnUiThread(Runnable {
                        view.updateWhoListSelected(personList)
                    })
                }
                activeJobCount -= 1
                view.showBaseProgressBar(activeJobCount > 0)
            }

        }

    }

    @JsName("handleDialogFromCalendarSelected")
    fun handleDialogFromCalendarSelected(year: Int, month: Int, dayOfMonth: Int) {
        fromDateTime = UMCalendarUtil.setDate(year, month, dayOfMonth)
        handleFromCalendarSelected()
    }

    @JsName("handleFromCalendarSelected")
    fun handleFromCalendarSelected() {
        view.runOnUiThread(Runnable { view.updateFromDialogText(fromDateTime.format("dd/MM/YYYY")) })
    }

    @JsName("handleDialogToCalendarSelected")
    fun handleDialogToCalendarSelected(year: Int, month: Int, dayOfMonth: Int) {
        toDateTime = UMCalendarUtil.setDate(year, month, dayOfMonth)
        handleToCalendarSelected()
    }

    @JsName("handleToCalendarSelected")
    fun handleToCalendarSelected() {
        view.runOnUiThread(Runnable { view.updateToDialogText(toDateTime.format("dd/MM/YYYY")) })
    }

    @JsName("handleDateRangeSelected")
    fun handleDateRangeSelected() {
        fromDateTimemillis = fromDateTime.unixMillisLong
        toDateTimeMillis = toDateTime.unixMillisLong
        view.runOnUiThread(Runnable {
            view.updateWhenRangeText(
                    fromDateTime.format("dd MMM yyyy") + " - " + toDateTime.format("dd MMM yyyy"))
        })
    }

    @JsName("handleWhoDataTyped")
    fun handleWhoDataTyped(name: String, uidList: List<Long>) {
        activeJobCount += 1
        view.showBaseProgressBar(activeJobCount > 0)
        GlobalScope.launch {
            val personsNames = personDao.getAllPersons("%$name%", uidList)
            view.runOnUiThread(Runnable {
                view.updateWhoDataAdapter(personsNames)
                activeJobCount -= 1
                view.showBaseProgressBar(activeJobCount > 0)
            })
        }
    }

    @JsName("handleDidDataTyped")
    fun handleDidDataTyped(verb: String, uidList: List<Long>) {
        activeJobCount++
        view.showBaseProgressBar(activeJobCount > 0)
        GlobalScope.launch {
            val verbs = xLangMapEntryDao.getAllVerbs("%$verb%", uidList)
            view.runOnUiThread(Runnable {
                view.updateDidDataAdapter(verbs)
                activeJobCount--
                view.showBaseProgressBar(activeJobCount > 0)
            })

        }
    }

    @JsName("handleWhereClicked")
    fun handleWhereClicked() {
        val args = mutableMapOf<String, String>()
        args[ARG_LOCATIONS_SET] = selectedLocations.joinToString { it.toString() }
        impl.go(SelectMultipleLocationTreeDialogView.VIEW_NAME, args, context)
    }


    @JsName("handleWhatClicked")
    fun handleWhatClicked() {
        val args = mutableMapOf<String, String?>()
        args.putAll(arguments)
        args[ARG_CONTENT_ENTRY_SET] = selectedEntries.joinToString { it.toString() }
        impl.go(SelectMultipleEntriesTreeDialogView.VIEW_NAME, args, context)
    }

    @JsName("handleViewReportPreview")
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
        args.putAll(arguments)
        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptions!!)
        impl.go(XapiReportDetailView.VIEW_NAME, args, context)
    }

    @JsName("handleLocationListSelected")
    fun handleLocationListSelected(locationList: List<Long>) {
        selectedLocations = locationList
    }

    @JsName("handleEntriesListSelected")
    fun handleEntriesListSelected(entriesList: List<Long>) {
        selectedEntries = entriesList
        GlobalScope.launch {
            selectedObjects = xObjectDao.findListOfObjectUidFromContentEntryUid(selectedEntries)
        }
    }

    @JsName("handleSelectedYAxis")
    fun handleSelectedYAxis(position: Int) {
        selectedYaxis = position
    }

    @JsName("handleSelectedChartType")
    fun handleSelectedChartType(position: Int) {
        selectedChartType = position
    }

    @JsName("handleSelectedXAxis")
    fun handleSelectedXAxis(position: Int) {
        selectedXAxis = position
    }

    @JsName("handleSelectedSubGroup")
    fun handleSelectedSubGroup(position: Int) {
        selectedSubGroup = position
    }


}