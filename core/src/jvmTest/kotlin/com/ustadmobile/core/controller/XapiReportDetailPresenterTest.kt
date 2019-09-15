package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.soywiz.klock.DateTime
import com.ustadmobile.core.controller.XapiReportDetailPresenter.Companion.HRS
import com.ustadmobile.core.controller.XapiReportDetailPresenter.Companion.MINS
import com.ustadmobile.core.controller.XapiReportDetailPresenter.Companion.SECS
import com.ustadmobile.core.controller.XapiReportOptions.Companion.AVG_DURATION
import com.ustadmobile.core.controller.XapiReportOptions.Companion.COUNT_ACTIVITIES
import com.ustadmobile.core.controller.XapiReportOptions.Companion.DURATION
import com.ustadmobile.core.controller.XapiReportOptions.Companion.SCORE
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.XapiReportDetailView
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

class TestXapiReportDetailPresenter : AbstractXapiReportOptionsTest() {

    private lateinit var mockImpl: UstadMobileSystemImpl

    private lateinit var mockView: XapiReportDetailView

    private lateinit var context: Any

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var reportOptionsWithDataFilled: XapiReportOptions

    private lateinit var reportOptionsWithNoData: XapiReportOptions

    private lateinit var reportOptionsWithTotalDurationLabels: XapiReportOptions

    private lateinit var reportOptionsWithAvgDurationLabels: XapiReportOptions


    private var stringMap = mapOf(MessageID.xapi_score to "Score (%)", DURATION to "Total Duration",
            AVG_DURATION to "Average Duration", SECS to "Secs", MINS to "Mins", HRS to "Hrs",
            COUNT_ACTIVITIES to "# of activities", MessageID.male to "Male", MessageID.female to "Female",
            MessageID.other to "Other", MessageID.unset to "Unset")


    @Before
    fun setup() {
        checkJndiSetup()
        context = Any()
        try {
            db = UmAppDatabase.getInstance(context)
            repo = db//.getUmRepository("http://localhost/dummy/", "")
            db.clearAllTables()

            insertXapi(db)

            reportOptionsWithDataFilled = XapiReportOptions(XapiReportOptions.BAR_CHART, SCORE, XapiReportOptions.MONTH, XapiReportOptions.GENDER, listOf(100), listOf(200), listOf(300), listOf(400),
                    DateTime(2019, 4, 10).unixMillisLong, DateTime(2019, 6, 11).unixMillisLong)

            reportOptionsWithNoData = XapiReportOptions(XapiReportOptions.BAR_CHART, COUNT_ACTIVITIES, XapiReportOptions.WEEK, XapiReportOptions.CONTENT_ENTRY)


            reportOptionsWithTotalDurationLabels = XapiReportOptions(XapiReportOptions.BAR_CHART, DURATION, XapiReportOptions.GENDER, XapiReportOptions.MONTH)

            reportOptionsWithAvgDurationLabels = XapiReportOptions(XapiReportOptions.BAR_CHART, AVG_DURATION, XapiReportOptions.DAY, XapiReportOptions.MONTH)


            mockImpl = spy()
            mockView = Mockito.mock(XapiReportDetailView::class.java)

            doAnswer {
                Thread(it.arguments[0] as Runnable).run()
                return@doAnswer // or you can type return@doAnswer null ​
            }.`when`(mockView).runOnUiThread(any())

            doAnswer {
                return@doAnswer stringMap[it.arguments[0] as Int]
            }.whenever(mockImpl).getString(any(), eq(context))


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    @Test
    fun givenOptionsWithAverageDurationSelected_showAverageDurationLabel() {

        val args = Hashtable<String, String>()
        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithAvgDurationLabels)

        val presenter = XapiReportDetailPresenter(context,
                args, mockView, mockImpl, repo.statementDao, repo.xLangMapEntryDao)
        presenter.onCreate(args)

        verify(mockView, timeout(15000)).setChartYAxisLabel("Average Duration (Hrs)")
        verify(mockView, timeout(15000)).setChartData(any(), any(),
                eq(mapOf("01 05 2019" to "01 05 2019",
                        "10 04 2019" to "10 04 2019",
                        "10 07 2019" to "10 07 2019",
                        "11 06 2019" to "11 06 2019",
                        "25 05 2019" to "25 05 2019",
                        "30 06 2019" to "30 06 2019")),
                eq(mapOf("05 2019" to "05 2019",
                        "04 2019" to "04 2019",
                        "07 2019" to "07 2019",
                        "06 2019" to "06 2019")))
        verify(mockView, timeout(15000)).setReportListData(any())


    }


    @Test
    fun givenOptionsWithTotalDurationSelected_showTotalDurationLabel() {

        val args = Hashtable<String, String>()
        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithTotalDurationLabels)

        val presenter = XapiReportDetailPresenter(context,
                args, mockView, mockImpl, repo.statementDao, repo.xLangMapEntryDao)
        presenter.onCreate(args)

        verify(mockView, timeout(15000)).setChartYAxisLabel("Total Duration (Hrs)")
        verify(mockView, timeout(15000)).setChartData(any(), any(),
                eq(mapOf("2" to "Male",
                        "1" to "Female",
                        "4" to "Other")),
                eq(mapOf("07 2019" to "07 2019",
                        "04 2019" to "04 2019",
                        "06 2019" to "06 2019",
                        "05 2019" to "05 2019")))
        verify(mockView, timeout(15000)).setReportListData(any())


    }

    @Test
    fun givenOptionsWithCountActivitySelected_showCountActivityLabel() {

        val args = Hashtable<String, String>()
        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithNoData)

        val presenter = XapiReportDetailPresenter(context,
                args, mockView, mockImpl, repo.statementDao, repo.xLangMapEntryDao)
        presenter.onCreate(args)


        verify(mockView, timeout(15000)).setChartYAxisLabel("# of activities")
        verify(mockView, timeout(15000)).setChartData(any(), any(),
                eq(mapOf("07 04 2019" to "07 04 2019",
                        "07 07 2019" to "07 07 2019",
                        "09 06 2019" to "09 06 2019",
                        "19 05 2019" to "19 05 2019",
                        "28 04 2019" to "28 04 2019",
                        "30 06 2019" to "30 06 2019")),
                eq(mapOf("300" to "Answer",
                        "301" to "Me")))
        verify(mockView, timeout(15000)).setReportListData(any())

    }

    @Test
    fun givenOptionsWithScoreSelected_showScoreLabel() {

        val args = Hashtable<String, String>()
        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithDataFilled)

        val presenter = XapiReportDetailPresenter(context,
                args, mockView, mockImpl, repo.statementDao, repo.xLangMapEntryDao)
        presenter.onCreate(args)

        verify(mockView, timeout(15000)).setChartYAxisLabel("Score (%)")
        verify(mockView, timeout(15000)).setChartData(any(), any(),
                eq(mapOf("06 2019" to "06 2019")),
                eq(mapOf("2" to "Male")))
        verify(mockView, timeout(15000)).setReportListData(any())

    }


}