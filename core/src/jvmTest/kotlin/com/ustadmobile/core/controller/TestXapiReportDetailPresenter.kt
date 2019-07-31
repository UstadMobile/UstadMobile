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
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.XapiReportDetailView
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_FEMALE
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_MALE
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_OTHER
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_UNSET
import com.ustadmobile.util.test.AbstractXapiReportOptionsTest
import com.ustadmobile.util.test.checkJndiSetup
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


    private var stringMap = mapOf(SCORE to "Score (%)", DURATION to "Total Duration",
            AVG_DURATION to "Average Duration", SECS to "Secs", MINS to "Mins", HRS to "Hrs",
            COUNT_ACTIVITIES to "# of activities", GENDER_MALE to "Male", GENDER_FEMALE to "Female",
            GENDER_OTHER to "Other", GENDER_UNSET to "Unset")


    @Before
    fun setup() {
        checkJndiSetup()
        context = Any()
        try {
            db = UmAppDatabase.getInstance(context)
            repo = db//.getRepository("http://localhost/dummy/", "")
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
                args, mockView, mockImpl)
        presenter.onCreate(args)

        verify(mockView, timeout(15000)).setChartYAxisLabel("Average Duration (Hrs)")


    }


    @Test
    fun givenOptionsWithTotalDurationSelected_showTotalDurationLabel() {

        val args = Hashtable<String, String>()
        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithTotalDurationLabels)

        val presenter = XapiReportDetailPresenter(context,
                args, mockView, mockImpl)
        presenter.onCreate(args)

        verify(mockView, timeout(15000)).setChartYAxisLabel("Total Duration (Hrs)")

    }

    @Test
    fun givenOptionsWithCountActivitySelected_showCountActivityLabel() {

        val args = Hashtable<String, String>()
        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithNoData)

        val presenter = XapiReportDetailPresenter(context,
                args, mockView, mockImpl)
        presenter.onCreate(args)

        verify(mockView, timeout(15000)).setChartYAxisLabel("# of activities")

    }

    @Test
    fun givenOptionsWithScoreSelected_showScoreLabel() {

        val args = Hashtable<String, String>()
        args[XapiReportDetailView.ARG_REPORT_OPTIONS] = Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithDataFilled)

        val presenter = XapiReportDetailPresenter(context,
                args, mockView, mockImpl)
        presenter.onCreate(args)

        verify(mockView, timeout(15000)).setChartYAxisLabel("Score (%)")

    }


}