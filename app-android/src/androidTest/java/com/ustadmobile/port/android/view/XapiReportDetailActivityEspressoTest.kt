package com.ustadmobile.port.android.view

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.XapiReportOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.XapiReportDetailView
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.test.core.impl.ProgressIdlingResource
import com.ustadmobile.test.port.android.UmAndroidTestUtil
import com.ustadmobile.test.port.android.UmAndroidTestUtil.childAtPosition
import com.ustadmobile.util.test.AbstractXapiReportOptionsTest
import junit.framework.Assert.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

class XapiReportDetailActivityEspressoTest : AbstractXapiReportOptionsTest() {

    @get:Rule
    val mActivityRule = IntentsTestRule(XapiReportDetailActivity::class.java, false, false)

    private var context = InstrumentationRegistry.getInstrumentation().context

    private lateinit var reportOptionsWithNoFilters: XapiReportOptions

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var reportOptionsWithDataFilled: XapiReportOptions

    private lateinit var activity: XapiReportDetailActivity

    private var idleProgress: ProgressIdlingResource? = null


    @Before
    fun setup() {
        UstadMobileSystemImpl.instance.messageIdMap = MessageIDMap.ID_MAP
        db = UmAppDatabase.getInstance(context)
        repo = db //db!!.getUmRepository("http://localhost/dummy/", "")
        db.clearAllTables()

        insertXapi(db)

        reportOptionsWithDataFilled = XapiReportOptions(XapiReportOptions.LINE_GRAPH, XapiReportOptions.SCORE, XapiReportOptions.MONTH, XapiReportOptions.GENDER, listOf(100), listOf(200), listOf(300), listOf(400),
                DateTime(2019, 4, 10).unixMillisLong, DateTime(2019, 6, 11).unixMillisLong)

        reportOptionsWithNoFilters = XapiReportOptions(XapiReportOptions.BAR_CHART, XapiReportOptions.SCORE, XapiReportOptions.MONTH, XapiReportOptions.CONTENT_ENTRY)
    }

    @After
    fun close() {
        IdlingRegistry.getInstance().unregister(idleProgress)
    }


    @Test
    fun givenDataWithNoFilters_whenActivityLaunches_thenDisplayDataWithChartAndList() {
        val intent = Intent()
        intent.putExtra(XapiReportDetailView.ARG_REPORT_OPTIONS,
                Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithNoFilters))
        mActivityRule.launchActivity(intent)
        activity = mActivityRule.activity

        idleProgress = ProgressIdlingResource(activity)

        IdlingRegistry.getInstance().register(idleProgress)

        IdlingPolicies.setMasterPolicyTimeout(3, TimeUnit.MINUTES)
        IdlingPolicies.setIdlingResourceTimeout(3, TimeUnit.MINUTES)

        Espresso.onView(ViewMatchers.withId(R.id.preview_chart_view)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        val textView2 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withText("Preview"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.um_toolbar),
                                        childAtPosition(
                                                ViewMatchers.withId(R.id.preview_toolbar),
                                                0)),
                                1),
                        ViewMatchers.isDisplayed()))
        textView2.check(ViewAssertions.matches(ViewMatchers.withText("Preview")))

        val textView = Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.preview_ylabel), ViewMatchers.isDisplayed()))
        textView.check(ViewAssertions.matches(ViewMatchers.withText("Score (%)")))

        val textView3 = Espresso.onView(ViewMatchers.withId(R.id.xapi_person_header))
        textView3.check(ViewAssertions.matches(ViewMatchers.withText("Person")))

        val textView4 = Espresso.onView(ViewMatchers.withId(R.id.xapi_verb_header))
        textView4.check(ViewAssertions.matches(ViewMatchers.withText("Did what")))

        val textView5 = Espresso.onView(ViewMatchers.withId(R.id.xapi_result_header))
        textView5.check(ViewAssertions.matches(ViewMatchers.withText("Result")))

        val textView6 = Espresso.onView(ViewMatchers.withId(R.id.xapi_when_header))
        textView6.check(ViewAssertions.matches(ViewMatchers.withText("When")))

        assertTrue(activity.findViewById<RecyclerView>(R.id.preview_report_list).adapter!!.itemCount > 5)

    }


    @Test
    fun givenDataWithFilters_whenActivityLaunches_thenDisplayDataWithChartAndList() {
        val intent = Intent()
        intent.putExtra(XapiReportDetailView.ARG_REPORT_OPTIONS,
                Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithDataFilled))
        mActivityRule.launchActivity(intent)
        activity = mActivityRule.activity

        idleProgress = ProgressIdlingResource(activity)

        IdlingRegistry.getInstance().register(idleProgress)

        Espresso.onView(ViewMatchers.withId(R.id.preview_chart_view)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        val textView2 = Espresso.onView(
                Matchers.allOf(ViewMatchers.withText("Preview"),
                        childAtPosition(
                                Matchers.allOf(ViewMatchers.withId(R.id.um_toolbar),
                                        childAtPosition(
                                                ViewMatchers.withId(R.id.preview_toolbar),
                                                0)),
                                1),
                        ViewMatchers.isDisplayed()))
        textView2.check(ViewAssertions.matches(ViewMatchers.withText("Preview")))

        val textView = Espresso.onView(Matchers.allOf(ViewMatchers.withId(R.id.preview_ylabel), ViewMatchers.isDisplayed()))
        textView.check(ViewAssertions.matches(ViewMatchers.withText("Score (%)")))

        val textView3 = Espresso.onView(ViewMatchers.withId(R.id.xapi_person_header))
        textView3.check(ViewAssertions.matches(ViewMatchers.withText("Person")))

        val textView4 = Espresso.onView(ViewMatchers.withId(R.id.xapi_verb_header))
        textView4.check(ViewAssertions.matches(ViewMatchers.withText("Did what")))

        val textView5 = Espresso.onView(ViewMatchers.withId(R.id.xapi_result_header))
        textView5.check(ViewAssertions.matches(ViewMatchers.withText("Result")))

        val textView6 = Espresso.onView(ViewMatchers.withId(R.id.xapi_when_header))
        textView6.check(ViewAssertions.matches(ViewMatchers.withText("When")))

        assertTrue(activity.findViewById<RecyclerView>(R.id.preview_report_list).adapter!!.itemCount > 0)

    }

}