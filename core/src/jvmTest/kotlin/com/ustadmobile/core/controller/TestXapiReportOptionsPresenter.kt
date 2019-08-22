package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.soywiz.klock.DateTime
import com.ustadmobile.core.controller.XapiReportOptions.Companion.BAR_CHART
import com.ustadmobile.core.controller.XapiReportOptions.Companion.CONTENT_ENTRY
import com.ustadmobile.core.controller.XapiReportOptions.Companion.GENDER
import com.ustadmobile.core.controller.XapiReportOptions.Companion.MONTH
import com.ustadmobile.core.controller.XapiReportOptions.Companion.SCORE
import com.ustadmobile.core.controller.XapiReportOptions.Companion.WEEK
import com.ustadmobile.core.controller.XapiReportOptions.Companion.listOfGraphs
import com.ustadmobile.core.controller.XapiReportOptions.Companion.xAxisList
import com.ustadmobile.core.controller.XapiReportOptions.Companion.yAxisList
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.XLangMapEntryDao
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SelectMultipleEntriesTreeDialogView
import com.ustadmobile.core.view.XapiReportDetailView
import com.ustadmobile.core.view.XapiReportOptionsView
import com.ustadmobile.util.test.AbstractXapiReportOptionsTest
import com.ustadmobile.util.test.checkJndiSetup
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.*
import javax.xml.bind.JAXBElement

class TestXapiReportOptionsPresenter : AbstractXapiReportOptionsTest() {


    private lateinit var reportOptionsWithNoData: XapiReportOptions

    private lateinit var mockView: XapiReportOptionsView

    private lateinit var context: Any

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var reportOptionsWithDataFilled: XapiReportOptions

    private lateinit var mockImpl: UstadMobileSystemImpl

    @Before
    fun setup() {
        checkJndiSetup()

        context = Any()
        try {
            db = UmAppDatabase.getInstance(context)
            repo = db//.getRepository("http://localhost/dummy/", "")
            db.clearAllTables()

            insertXapi(db)

            reportOptionsWithDataFilled = XapiReportOptions(BAR_CHART, SCORE, MONTH, GENDER, listOf(100), listOf(200), listOf(300), listOf(400),
                    DateTime(2019, 4, 10).unixMillisLong, DateTime(2019, 6, 11).unixMillisLong)

            reportOptionsWithNoData = XapiReportOptions(BAR_CHART, SCORE, WEEK, CONTENT_ENTRY)

            mockImpl = spy()
            UstadMobileSystemImpl.instance = mockImpl
            mockView = Mockito.mock(XapiReportOptionsView::class.java)

            doAnswer {
                Thread(it.arguments[0] as Runnable).run()
                return@doAnswer // or you can type return@doAnswer null ​
            }.`when`(mockView).runOnUiThread(any())


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Test
    fun givenReportOptionsWithNoData_thenVerifySpinnerWasPrefilled() {

        val args = Hashtable<String, String>()
        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithNoData)

        val presenter = XapiReportOptionsPresenter(context,
                args, mockView)
        presenter.onCreate(args)

        verify(mockView, timeout(15000)).updateChartTypeSelected(listOfGraphs.indexOf(BAR_CHART))
        verify(mockView, timeout(15000)).updateYAxisTypeSelected(yAxisList.indexOf(SCORE))
        verify(mockView, timeout(15000)).updateXAxisTypeSelected(xAxisList.indexOf(WEEK))
        verify(mockView, timeout(15000)).updateSubgroupTypeSelected(xAxisList.indexOf(CONTENT_ENTRY))
    }

    @Test
    fun givenReportOptionsWithDataFilled_thenVerify() {

        val args = Hashtable<String, String>()
        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithDataFilled)

        GlobalScope.launch {
            val verbs = db.xLangMapEntryDao.getAllVerbsInList(reportOptionsWithDataFilled.didFilterList)
            val persons = db.personDao.getAllPersonsInList(reportOptionsWithDataFilled.whoFilterList)
            val presenter = XapiReportOptionsPresenter(context,
                    args, mockView)
            presenter.onCreate(args)

            verify(mockView, timeout(15000)).updateChartTypeSelected(listOfGraphs.indexOf(BAR_CHART))
            verify(mockView, timeout(15000)).updateYAxisTypeSelected(yAxisList.indexOf(SCORE))
            verify(mockView, timeout(15000)).updateXAxisTypeSelected(xAxisList.indexOf(MONTH))
            verify(mockView, timeout(15000)).updateSubgroupTypeSelected(xAxisList.indexOf(GENDER))
            verify(mockView, timeout(15000)).updateDidListSelected(verbs)
            verify(mockView, timeout(15000)).updateWhoListSelected(persons)
        }


    }

    @Test
    fun givenReportOptionsWithData_whenNoChanges_thenHandleReportReviewWithSameData() {

        val args = HashMap<String, String>()
        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithDataFilled)

        val presenter = XapiReportOptionsPresenter(context,
                args, mockView)
        presenter.onCreate(args)

        presenter.handleViewReportPreview(reportOptionsWithDataFilled.didFilterList, reportOptionsWithDataFilled.whoFilterList)

        verify(mockImpl).go(XapiReportDetailView.VIEW_NAME, args, context)

    }


    @Test
    fun givenReportOptionsWithData_whenDatesAreChanged_thenHandleReportReviewWithChangedDates() {

        val args = Hashtable<String, String>()
        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithNoData)

        val presenter = XapiReportOptionsPresenter(context,
                args, mockView)
        presenter.onCreate(args)

        presenter.handleFromCalendarSelected(2018, 6, 10)
        presenter.handleToCalendarSelected(2019, 8, 19)
        reportOptionsWithNoData.fromDate = DateTime(2018, 6, 10).unixMillisLong
        reportOptionsWithNoData.toDate = DateTime(2019, 8, 19).unixMillisLong

        verify(mockView, timeout(15000)).updateFromDialogText("10/06/2018")
        verify(mockView, timeout(15000)).updateToDialogText("19/08/2019")

        presenter.handleDateRangeSelected()

        verify(mockView, timeout(15000)).updateWhenRangeText("10 Jun 2018 - 19 Aug 2019")

        presenter.handleViewReportPreview(reportOptionsWithNoData.didFilterList, reportOptionsWithNoData.whoFilterList)
        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithNoData)
        verify(mockImpl).go(XapiReportDetailView.VIEW_NAME, args, context)

    }


    @Test
    fun givenReportOptionsWithData_whenSpinnerDataChanges_thenHandleReportReviewWithSpinnerDataChanged() {

        val args = HashMap<String, String>()
        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithDataFilled)

        val presenter = XapiReportOptionsPresenter(context,
                args, mockView)
        presenter.onCreate(args)

        presenter.handleSelectedYAxis(1)
        reportOptionsWithDataFilled.yAxis = yAxisList[1]

        presenter.handleSelectedChartType(1)
        reportOptionsWithDataFilled.chartType = listOfGraphs[1]

        presenter.handleSelectedXAxis(3)
        reportOptionsWithDataFilled.xAxis = xAxisList[3]

        presenter.handleSelectedSubGroup(1)
        reportOptionsWithDataFilled.subGroup = xAxisList[1]

        presenter.handleViewReportPreview(reportOptionsWithDataFilled.didFilterList, reportOptionsWithDataFilled.whoFilterList)

        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithDataFilled)
        verify(mockImpl).go(XapiReportDetailView.VIEW_NAME, args, context)

    }

    @Test
    fun givenReportOptionsWithData_whenWhatDataChanged_thenHandleReportReviewWithWhatDataChanged() {

        val args = HashMap<String, String>()
        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithDataFilled)

        val presenter = XapiReportOptionsPresenter(context,
                args, mockView)
        presenter.onCreate(args)

        presenter.handleEntriesListSelected(listOf())
        reportOptionsWithDataFilled.entriesList = listOf()
        reportOptionsWithDataFilled.objectsList = listOf()

        GlobalScope.launch {
            presenter.handleViewReportPreview(reportOptionsWithDataFilled.didFilterList, reportOptionsWithDataFilled.whoFilterList)

            args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithDataFilled)
            verify(mockImpl).go(XapiReportDetailView.VIEW_NAME, args, context)
        }

    }


    @Test
    fun givenReportOptionsWithData_thenWhenHandleWhatClicked_verifyGoToFragment() {

        val args = Hashtable<String, String>()
        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithDataFilled)

        val presenter = XapiReportOptionsPresenter(context,
                args, mockView)
        presenter.onCreate(args)

        presenter.handleWhatClicked()

        val treeMap = mutableMapOf<String, String>()
        treeMap[SelectMultipleEntriesTreeDialogView.ARG_CONTENT_ENTRY_SET] = reportOptionsWithDataFilled.entriesList.joinToString { it.toString() }

        verify(mockImpl).go(SelectMultipleEntriesTreeDialogView.VIEW_NAME, treeMap, context)
    }

    @Test
    fun givenPersonName_whenNothingSelected_thenReturnFullListToActivity() {
        val presenter = XapiReportOptionsPresenter(context, mapOf(), mockView)
        presenter.onCreate(null)

        presenter.handleWhoDataTyped("He", listOf())

        verify(mockView, timeout(15000)).updateWhoDataAdapter(
                listOf(PersonDao.PersonNameAndUid(100, "Hello World"), PersonDao.PersonNameAndUid(101, "Here Now")))

    }

    @Test
    fun givenPersonName_whenNamePreviouslySelected_thenReturnFilteredListToActivity() {
        val presenter = XapiReportOptionsPresenter(context, mapOf(), mockView)
        presenter.onCreate(null)

        presenter.handleWhoDataTyped("He", listOf(100))

        verify(mockView, timeout(15000)).updateWhoDataAdapter(
                listOf(PersonDao.PersonNameAndUid(101, "Here Now")))

    }


    @Test
    fun givenVerb_whenNothingSelected_thenReturnFullListToActivity() {
        val presenter = XapiReportOptionsPresenter(context, mapOf(), mockView)
        presenter.onCreate(null)

        presenter.handleDidDataTyped("Attemp", listOf())

        verify(mockView, timeout(15000)).updateDidDataAdapter(
                listOf(XLangMapEntryDao.Verb(200, "Attempted question 3 from Entry 1"),
                        XLangMapEntryDao.Verb(201, "Attempted question 1 from Entry 1"),
                        XLangMapEntryDao.Verb(202, "Attempted question 5 from Entry 3")))

    }

    @Test
    fun givenVerb_whenNamePreviouslySelected_thenReturnFilteredListToActivity() {
        val presenter = XapiReportOptionsPresenter(context, mapOf(), mockView)
        presenter.onCreate(null)

        presenter.handleDidDataTyped("Attemp", listOf(201))

        verify(mockView, timeout(15000)).updateDidDataAdapter(
                listOf(XLangMapEntryDao.Verb(200, "Attempted question 3 from Entry 1"),
                        XLangMapEntryDao.Verb(202, "Attempted question 5 from Entry 3")))

    }


}