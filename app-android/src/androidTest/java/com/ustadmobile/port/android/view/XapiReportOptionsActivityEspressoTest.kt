package com.ustadmobile.port.android.view

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.platform.app.InstrumentationRegistry
import com.soywiz.klock.DateTime
import com.ustadmobile.core.controller.XapiReportOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.XapiReportOptionsView
import com.ustadmobile.port.android.generated.MessageIDMap
import com.ustadmobile.util.test.AbstractXapiReportOptionsTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import com.google.android.flexbox.FlexboxLayout
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.XapiReportDetailView
import junit.framework.Assert.assertEquals
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.hamcrest.CoreMatchers.*
import com.ustadmobile.test.port.android.UmAndroidTestUtil
import com.ustadmobile.test.port.android.UmAndroidTestUtil.childAtPosition
import org.hamcrest.Matchers
import org.hamcrest.core.IsInstanceOf
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.Espresso.onView
import junit.framework.Assert.assertTrue


class XapiReportOptionsActivityEspressoTest : AbstractXapiReportOptionsTest() {

    @get:Rule
    val mActivityRule = IntentsTestRule(XapiReportOptionsActivity::class.java, false, false)

    private var context = InstrumentationRegistry.getInstrumentation().context

    private lateinit var reportOptionsWithNoFilters: XapiReportOptions

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var reportOptionsWithDataFilled: XapiReportOptions


    private lateinit var activity: XapiReportOptionsActivity


    @Before
    fun setup() {
        UstadMobileSystemImpl.instance.messageIdMap = MessageIDMap.ID_MAP
        db = UmAppDatabase.getInstance(context)
        repo = db //db!!.getRepository("http://localhost/dummy/", "")
        db.clearAllTables()

        insertXapi(db)

        reportOptionsWithDataFilled = XapiReportOptions(XapiReportOptions.LINE_GRAPH, XapiReportOptions.SCORE, XapiReportOptions.MONTH, XapiReportOptions.GENDER, listOf(100), listOf(200), listOf(300), listOf(400),
                DateTime(2019, 4, 10).unixMillisLong, DateTime(2019, 6, 11).unixMillisLong)

        reportOptionsWithNoFilters = XapiReportOptions(XapiReportOptions.BAR_CHART, XapiReportOptions.SCORE, XapiReportOptions.MONTH, XapiReportOptions.CONTENT_ENTRY)
    }

    @Test
    fun givenDefaultData_whenActivityLaunches_thenDisplayDefaultData() {
        val intent = Intent()
        mActivityRule.launchActivity(intent)
        activity = mActivityRule.activity

        onView(withId(R.id.type_spinner)).check(matches(withSpinnerText(containsString("Bar Chart"))))
        onView(withId(R.id.yaxis_spinner)).check(matches(withSpinnerText(containsString("Average Score"))))
        onView(withId(R.id.xaxis_spinner)).check(matches(withSpinnerText(containsString("Day"))))
        onView(withId(R.id.sub_group_spinner)).check(matches(withSpinnerText(containsString("Day"))))

        val whoFlex = activity.findViewById<FlexboxLayout>(R.id.whoFlex)
        assertEquals(1, whoFlex.childCount)

        val didFlex = activity.findViewById<FlexboxLayout>(R.id.didFlex)
        assertEquals(1, didFlex.childCount)

        onView(withId(R.id.whenEditText)).check(matches(withText("")))

    }

    @Test
    fun givenDataWithNoFilters_whenActivityLaunches_thenDisplayDataWithNoFilters() {
        val intent = Intent()
        intent.putExtra(XapiReportDetailView.ARG_REPORT_OPTIONS,
                Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithNoFilters))
        mActivityRule.launchActivity(intent)
        activity = mActivityRule.activity

        onView(withId(R.id.type_spinner)).check(matches(withSpinnerText(containsString("Bar Chart"))))
        onView(withId(R.id.yaxis_spinner)).check(matches(withSpinnerText(containsString("Average Score"))))
        onView(withId(R.id.xaxis_spinner)).check(matches(withSpinnerText(containsString("Month"))))
        onView(withId(R.id.sub_group_spinner)).check(matches(withSpinnerText(containsString("Content Entry"))))

        val whoFlex = activity.findViewById<View>(R.id.whoFlex) as FlexboxLayout
        assertEquals(1, whoFlex.childCount)

        val didFlex = activity.findViewById<View>(R.id.didFlex) as FlexboxLayout
        assertEquals(1, didFlex.childCount)

        onView(withId(R.id.whenEditText)).check(matches(withText("")))
    }


    @Test
    fun givenDataWithFilters_whenActivityLaunches_thenDisplayDataWithFilters() {
        val intent = Intent()
        intent.putExtra(XapiReportDetailView.ARG_REPORT_OPTIONS,
                Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithDataFilled))
        mActivityRule.launchActivity(intent)
        activity = mActivityRule.activity

        onView(withId(R.id.type_spinner)).check(matches(withSpinnerText(containsString("Line Graph"))))
        onView(withId(R.id.yaxis_spinner)).check(matches(withSpinnerText(containsString("Average Score"))))
        onView(withId(R.id.xaxis_spinner)).check(matches(withSpinnerText(containsString("Month"))))
        onView(withId(R.id.sub_group_spinner)).check(matches(withSpinnerText(containsString("Gender"))))


        val whoFlex = activity.findViewById<View>(R.id.whoFlex) as FlexboxLayout
        assertEquals(2, whoFlex.childCount)

        val didFlex = activity.findViewById<View>(R.id.didFlex) as FlexboxLayout
        assertEquals(2, didFlex.childCount)

        onView(withId(R.id.whenEditText)).check(matches(withText("10 Apr 2019 - 11 Jun 2019")))

        val chip = onView(
                Matchers.allOf(childAtPosition(Matchers.allOf(withId(R.id.whoFlex),
                        childAtPosition(IsInstanceOf.instanceOf(ViewGroup::class.java), 12)),
                        0), isDisplayed()))
        chip.check(matches(withText("Hello World")))

        val didChip = onView(
                Matchers.allOf(childAtPosition(Matchers.allOf(withId(R.id.didFlex),
                        childAtPosition(IsInstanceOf.instanceOf(ViewGroup::class.java), 14)),
                        0), isDisplayed()))
        didChip.check(matches(withText("Attempted question 3 from Entry 1")))

    }


    @Test
    fun givenDataWithFilters_whenWhenEditTextIsClicked_thenShowDialogWithDatesAlreadySelected() {
        val intent = Intent()
        intent.putExtra(XapiReportDetailView.ARG_REPORT_OPTIONS,
                Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithDataFilled))
        mActivityRule.launchActivity(intent)
        activity = mActivityRule.activity

        UmAndroidTestUtil.swipeScreenDown()
        onView(withId(R.id.whenEditText)).perform(click())
        onView(Matchers.allOf(withId(R.id.fragment_select_daterange_dialog_from_time), isDisplayed())).check(matches(withText("10/04/2019")))
        onView(Matchers.allOf(withId(R.id.fragment_select_daterange_dialog_to_time), isDisplayed())).check(matches(withText("11/06/2019")))
    }

    @Test
    fun givenNoData_whenUserSearchForName_thenShowSuggestionsAndDisplayAsChipWhenSelected() {
        val intent = Intent()
        mActivityRule.launchActivity(intent)
        activity = mActivityRule.activity

        val appCompatAutoCompleteTextView = onView(
                Matchers.allOf(withId(R.id.whoAutoCompleteTextView), isDisplayed()))
        appCompatAutoCompleteTextView.perform(typeText("he"), typeText("l"), closeSoftKeyboard())


        val suggestion = onView(withText("Hello World"))
                .inRoot(withDecorView(not(`is`(activity.window.decorView))))
        suggestion.check(matches(isDisplayed()))
        suggestion.perform(click())


        val chip = onView(
                Matchers.allOf(childAtPosition(Matchers.allOf(withId(R.id.whoFlex),
                        childAtPosition(IsInstanceOf.instanceOf(ViewGroup::class.java), 12)),
                        0), isDisplayed()))
        chip.check(matches(withText("Hello World")))

        val didAutoComplete = onView(
                Matchers.allOf(withId(R.id.didAutoCompleteTextView), isDisplayed()))
        didAutoComplete.perform(typeText("att"), typeText("e"), closeSoftKeyboard())


        val didSuggest = onView(withText("Attempted question 5 from Entry 3"))
                .inRoot(withDecorView(not(`is`(activity.window.decorView))))
        didSuggest.check(matches(isDisplayed()))
        didSuggest.perform(click())


        val didChip = onView(
                Matchers.allOf(childAtPosition(Matchers.allOf(withId(R.id.didFlex),
                        childAtPosition(IsInstanceOf.instanceOf(ViewGroup::class.java), 14)),
                        0), isDisplayed()))
        didChip.check(matches(withText("Attempted question 5 from Entry 3")))
    }

    @Test
    fun givenWhatSelectionClicked_whenUserSelectsOptions_displayCorrectOptionsOnReturn() {
        val intent = Intent()
        mActivityRule.launchActivity(intent)
        activity = mActivityRule.activity

        val appCompatEditText = onView(
                Matchers.allOf(withId(R.id.whatEditText), isDisplayed()))
        appCompatEditText.perform(click())

        val constraintLayout = onView(
                Matchers.allOf(childAtPosition(
                        Matchers.allOf(withId(R.id.fragment_select_multiple_tree_dialog_recyclerview),
                                childAtPosition(withClassName(Matchers.`is`("androidx.constraintlayout.widget.ConstraintLayout")), 1)),
                        0), isDisplayed()))
        constraintLayout.perform(click())

        val constraintLayout2 = onView(
                Matchers.allOf(childAtPosition(
                        Matchers.allOf(withId(R.id.fragment_select_multiple_tree_dialog_recyclerview),
                                childAtPosition(withClassName(Matchers.`is`("androidx.constraintlayout.widget.ConstraintLayout")), 1)),
                        1), isDisplayed()))
        constraintLayout2.perform(click())

        val appCompatCheckBox = onView(
                Matchers.allOf(withId(R.id.item_select_multiple_tree_dialog_checkbox),
                        childAtPosition(childAtPosition(withId(R.id.fragment_select_multiple_tree_dialog_recyclerview), 2),
                                1), isDisplayed()))
        appCompatCheckBox.perform(click())

        val appCompatCheckBox2 = onView(
                Matchers.allOf(withId(R.id.item_select_multiple_tree_dialog_checkbox),
                        childAtPosition(
                                childAtPosition(withId(R.id.fragment_select_multiple_tree_dialog_recyclerview), 5), 1), isDisplayed()))
        appCompatCheckBox2.perform(click())

        val actionMenuItemView = onView(
                Matchers.allOf(withId(R.id.menu_done), withContentDescription("Done"),
                        childAtPosition(childAtPosition(withId(R.id.fragment_select_multiple_tree_dialog_toolbar), 1),
                                0), isDisplayed()))
        actionMenuItemView.perform(click())

        val editText = onView(
                Matchers.allOf(withId(R.id.whatEditText), withText("Class 1, EDRAAK"), isDisplayed()))
        editText.check(matches(withText("Class 1, EDRAAK")))

    }

    @Test
    fun givenNoDataFilled_whenUserClicksDone_checkAllElementsInNextPage() {
        val intent = Intent()
        intent.putExtra(XapiReportDetailView.ARG_REPORT_OPTIONS,
                Json(JsonConfiguration.Stable).stringify(XapiReportOptions.serializer(), reportOptionsWithNoFilters))
        mActivityRule.launchActivity(intent)
        activity = mActivityRule.activity

        val actionMenuItemView = onView(
                Matchers.allOf(withId(R.id.menu_done), withContentDescription("Done"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.new_report_toolbar),
                                        2),
                                0),
                        isDisplayed()))
        actionMenuItemView.perform(click())

        onView(withId(R.id.preview_chart_view)).check(matches(isDisplayed()))

        val textView2 = onView(
                Matchers.allOf(withText("Preview"),
                        childAtPosition(
                                Matchers.allOf(withId(R.id.preview_toolbar),
                                        childAtPosition(
                                                withId(R.id.new_report_collapsing_toolbar),
                                                0)),
                                1),
                        isDisplayed()))
        textView2.check(matches(withText("Preview")))

        val textView = onView(Matchers.allOf(withId(R.id.preview_ylabel), isDisplayed()))
        textView.check(matches(withText("Score (%)")))

        val textView3 = onView(withId(R.id.xapi_person_header))
        textView3.check(matches(withText("Person")))

        val textView4 = onView(withId(R.id.xapi_verb_header))
        textView4.check(matches(withText("Did what")))

        val textView5 = onView(withId(R.id.xapi_result_header))
        textView5.check(matches(withText("Result")))

        val textView6 = onView(withId(R.id.xapi_when_header))
        textView6.check(matches(withText("When")))

        val frameLayout = onView(withId(R.id.layout_button_container))
        frameLayout.check(matches(isDisplayed()))
        frameLayout.perform(click())

        val dialogTitle = onView(withId(R.id.alertTitle))
        dialogTitle.check(matches(withText("Report Title")))

        val editText = onView(
                Matchers.allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.custom),
                                0),
                        0),
                        isDisplayed()))
        editText.perform(typeText("Test Report"), closeSoftKeyboard())


        val button = onView(withId(android.R.id.button1))
        button.check(matches(withText("OK")))

        val button2 = onView(withId(android.R.id.button2))
        button2.check(matches(withText("Cancel")))
        button.perform(scrollTo(), click())

    }


}