package com.ustadmobile.port.android.view

import android.content.Context
import android.widget.DatePicker
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.toughra.ustadmobile.R
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecord
import com.ustadmobile.adbscreenrecorder.client.AdbScreenRecordRule
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.port.android.screen.MainActivityScreen
import com.ustadmobile.test.core.impl.CrudIdlingResource
import com.ustadmobile.test.core.impl.DataBindingIdlingResource
import com.ustadmobile.test.rules.*
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@AdbScreenRecord("Class end-to-end test")
class ClazzEndToEndTests {

    @JvmField
    @Rule
    var dbRule = UmAppDatabaseAndroidClientRule(useDbAsRepo = true)

    @JvmField
    @Rule
    val screenRecordRule = AdbScreenRecordRule()

    @AdbScreenRecord("Given an empty class list, when the user clicks add class and " +
            "fills in form, then it should go to the new class")
    @Test
    fun givenEmptyClazzList_whenUserClicksAddAndFillsInForm_thenClassIsCreatedAndGoneInto() {
        val calendarUid = dbRule.db.holidayCalendarDao.insert(HolidayCalendar().apply {
            this.umCalendarName = "Test Calendar"
        })

        dbRule.insertPersonForActiveUser(Person().apply {
            firstNames = "Bob"
            lastName = "Jones"
            admin = true
        })


        launchActivity<MainActivity>()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val newClazzText = context.getString(R.string.add_a_new,
                context.getString(R.string.clazz).toLowerCase())
        onView(withId(R.id.home_clazzlist_dest)).perform(click())
        onView(withText(R.string.clazz)).perform(click())
        onView(withText(newClazzText)).perform(click())
        onView(withId(R.id.activity_clazz_edit_name_text)).perform(typeText("Test Class"))
        closeSoftKeyboard()

        //select holiday calendar
        /*
         * Weird issue: if you specify the EditText itself, instead of the TextInputLayout, Android 5
         * will click ON 'SCHOOL' THE BOTTOM NAVIGATION instead!
         */
        onView(withId(R.id.activity_clazz_edit_holiday_calendar_selected)).perform(click())
        onView(withId(R.id.fragment_list_recyclerview)).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                        withTagValue(equalTo(calendarUid)),
                        click()))

        //Set the start date
        onView(withId(R.id.start_date_text)).perform(click())
        onView(withClassName(equalTo(DatePicker::class.java.name)))
                .perform(PickerActions.setDate(2020, 5, 31))
        onView(withId(android.R.id.button1)).perform(click())

        onView(withId(R.id.menu_done)).perform(click())


        val createdClazz = dbRule.db.clazzDao.findByClazzName("Test Class").first()
        onView(withText("Test Class")).perform(click())

        onView(allOf(withParent(withId(R.id.toolbar)), withText(createdClazz.clazzName))).check(matches(isDisplayed()))
    }

}