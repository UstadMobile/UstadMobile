package com.ustadmobile.port.android.view

import android.content.Intent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.platform.app.InstrumentationRegistry
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.spy
import com.soywiz.klock.DateTime
import com.ustadmobile.core.controller.XapiReportOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.XapiReportDetailView
import com.ustadmobile.core.view.XapiReportOptionsView
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.util.test.AbstractXapiReportOptionsTest
import com.ustadmobile.util.test.checkJndiSetup
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class NewReportEspressoTest: AbstractXapiReportOptionsTest() {

    @get:Rule
    val mActivityRule = IntentsTestRule(XapiReportOptionsActivity::class.java, false, false)

    private var context = InstrumentationRegistry.getInstrumentation().context

    private lateinit var reportOptionsWithNoData: XapiReportOptions

    private lateinit var mockView: XapiReportOptionsView

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var reportOptionsWithDataFilled: XapiReportOptions

    private lateinit var mockImpl: UstadMobileSystemImpl

    @Before
    fun setup() {
        checkJndiSetup()
        UstadMobileSystemImpl.instance.messageIdMap = MessageIDMap.ID_MAP
        db = UmAppDatabase.getInstance(context)
        repo = db //db!!.getRepository("http://localhost/dummy/", "")
        db.clearAllTables()

        insertXapi(db)

        reportOptionsWithDataFilled = XapiReportOptions(XapiReportOptions.BAR_CHART, XapiReportOptions.SCORE, XapiReportOptions.MONTH, XapiReportOptions.GENDER, listOf(100), listOf(200), listOf(300), listOf(400),
                DateTime(2019, 4, 10).unixMillisLong,  DateTime(2019, 6, 11).unixMillisLong)

        reportOptionsWithNoData = XapiReportOptions(XapiReportOptions.BAR_CHART, XapiReportOptions.SCORE, XapiReportOptions.WEEK, XapiReportOptions.CONTENT_ENTRY)

        mockImpl = spy()
        UstadMobileSystemImpl.instance = mockImpl
        mockView = Mockito.mock(XapiReportOptionsView::class.java)

        doAnswer {
            Thread(it.arguments[0] as Runnable).run()
            null// or you can type return@doAnswer null ​
        }.`when`(mockView).runOnUiThread(any())

        val intent = Intent()
        intent.putExtra(XapiReportDetailView.ARG_REPORT_OPTIONS,
                Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithDataFilled))
        mActivityRule.launchActivity(intent)

    }

    @Test
    fun test(){



    }


}