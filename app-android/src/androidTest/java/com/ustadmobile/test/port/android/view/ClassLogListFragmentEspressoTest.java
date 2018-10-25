package com.ustadmobile.test.port.android.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzListPresenter;
import com.ustadmobile.core.controller.ClazzLogDetailPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.port.android.view.ClazzDetailActivity;
import com.ustadmobile.port.android.view.ClazzLogDetailActivity;
import com.ustadmobile.test.port.android.testutil.UmDbTestUtil;

import java.util.Hashtable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.BundleMatchers.hasEntry;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtras;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.*;
import static com.ustadmobile.test.port.android.view.ClazzLogDetailActivityActivityEspressoTest.checkIconColorForStudent;
import static com.ustadmobile.test.port.android.view.ClazzLogDetailActivityActivityEspressoTest.checkStudentEntryIsDisplayed;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
import static android.support.test.espresso.assertion.ViewAssertions.matches;


/**
 * ClazzLogListFragment's Espresso UI Test for Android
 */
@RunWith(AndroidJUnit4.class)
public class ClassLogListFragmentEspressoTest {


    private static final String TEST_CLASS_NAME = "Class A";
    private static final float TEST_CLASS_PERCENTAGE = 0.42F;
    private static final long TEST_USER_UID = 1L;
    private static final String TEST_CLASS_MEMBER1_NAME = "Test User1";
    private static final float TEST_CLASS_MEMBER1_PERCENTAGE = 0.15F;
    private static final float TEST_CLASS_MEMBER2_PERCENTAGE = 0.35F;
    private static final float TEST_CLASS_MEMBER3_PERCENTAGE = 0.55F;
    private static final float TEST_CLASS_MEMBER4_PERCENTAGE = 0.85F;
    private static final String TEST_CLASS_MEMBER2_NAME = "Test User2";
    private static final String TEST_CLASS_MEMBER3_NAME = "Test User3";
    private static final String TEST_CLASS_MEMBER4_NAME = "Test User4";
    public static final String TEST_FEED_DESCRIPTION = "This is a test feed. Created from the tests.";
    public static final String TEST_FEED1_TITLE = "Take Attendance1 for Class A";
    public static final String TEST_FEED2_TITLE = "Take Attendance2 for Class A";
    public static long TEST_LOG_DATE1 = UMCalendarUtil.getDateInMilliPlusDays(0);
    public static long TEST_LOG_DATE2 = UMCalendarUtil.getDateInMilliPlusDays(-1);
    public static long TEST_LOG_DATE3 = UMCalendarUtil.getDateInMilliPlusDays(-2);
    public static long TEST_LOG_DATE4 = UMCalendarUtil.getDateInMilliPlusDays(-3);
    public static String TEST_PRETTY_DATE1 = UMCalendarUtil.getPrettyDateFromLong(TEST_LOG_DATE1);
    public static String TEST_PRETTY_DATE2 = UMCalendarUtil.getPrettyDateFromLong(TEST_LOG_DATE2);
    public static String TEST_PRETTY_DATE3 = UMCalendarUtil.getPrettyDateFromLong(TEST_LOG_DATE3);
    public static String TEST_PRETTY_DATE4 = UMCalendarUtil.getPrettyDateFromLong(TEST_LOG_DATE4);

    public static String TEST_PRETTY_DAY1 = UMCalendarUtil.getSimpleDayFromLongDate(TEST_LOG_DATE1);
    public static String TEST_PRETTY_DAY2 = UMCalendarUtil.getSimpleDayFromLongDate(TEST_LOG_DATE2);
    public static String TEST_PRETTY_DAY3 = UMCalendarUtil.getSimpleDayFromLongDate(TEST_LOG_DATE3);
    public static String TEST_PRETTY_DAY4 = UMCalendarUtil.getSimpleDayFromLongDate(TEST_LOG_DATE4);

    public static Long TEST_FEED1_LOGDATE = UMCalendarUtil.getDateInMilliPlusDays(1);
    public static Long TEST_FEED2_LOGDATE = UMCalendarUtil.getDateInMilliPlusDays(-1);

    private static Hashtable peopleMap;
    private static Hashtable feedsToCreate;
    private static Hashtable clazzLogsToCreate;
    private static Hashtable peopleAttendanceMap1;
    private static Hashtable peopleAttendanceMap2;

    //private static Hashtable
    private Clazz testClazz;

    static {
        peopleMap = new Hashtable();
        peopleMap.put(TEST_CLASS_MEMBER1_NAME, TEST_CLASS_MEMBER1_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER2_NAME, TEST_CLASS_MEMBER2_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER3_NAME, TEST_CLASS_MEMBER3_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER4_NAME, TEST_CLASS_MEMBER4_PERCENTAGE);

        feedsToCreate = new Hashtable();
        feedsToCreate.put(TEST_FEED1_TITLE, TEST_FEED1_LOGDATE);
        feedsToCreate.put(TEST_FEED2_TITLE, TEST_FEED2_LOGDATE);

        peopleAttendanceMap1 = new Hashtable();
        peopleAttendanceMap1.put(TEST_CLASS_MEMBER1_NAME, STATUS_ABSENT);
        peopleAttendanceMap1.put(TEST_CLASS_MEMBER2_NAME, STATUS_ATTENDED);
        peopleAttendanceMap1.put(TEST_CLASS_MEMBER3_NAME, STATUS_ATTENDED);
        peopleAttendanceMap1.put(TEST_CLASS_MEMBER4_NAME, STATUS_PARTIAL);

        peopleAttendanceMap2 = new Hashtable();
        peopleAttendanceMap1.put(TEST_CLASS_MEMBER1_NAME, STATUS_ABSENT);
        peopleAttendanceMap1.put(TEST_CLASS_MEMBER2_NAME, STATUS_ATTENDED);
        peopleAttendanceMap1.put(TEST_CLASS_MEMBER3_NAME, STATUS_ABSENT);
        peopleAttendanceMap1.put(TEST_CLASS_MEMBER4_NAME, STATUS_PARTIAL);

        clazzLogsToCreate = new Hashtable();
        clazzLogsToCreate.put(TEST_LOG_DATE1, new Hashtable());
        clazzLogsToCreate.put(TEST_LOG_DATE2, peopleAttendanceMap2);
        clazzLogsToCreate.put(TEST_LOG_DATE3, peopleAttendanceMap1);
        clazzLogsToCreate.put(TEST_LOG_DATE4, new Hashtable());
    }

    /**
     * This sets the activity that we want floating around
     */
    @Rule
    public IntentsTestRule<ClazzDetailActivity> mActivityRule =
            new IntentsTestRule<>(ClazzDetailActivity.class, false, false);

    @Before
    public void beforeTest() {
        Context context = InstrumentationRegistry.getContext();
        UmAppDatabase.getInstance(context).clearAllTables();

        //Populate the database
        testClazz = UmDbTestUtil.createClazzWithClazzMembers(TEST_CLASS_NAME, TEST_CLASS_PERCENTAGE,
                peopleMap, TEST_USER_UID, context);

        UmDbTestUtil.createClazzLogs(clazzLogsToCreate, testClazz, context);

        //Start the activity
        Intent launchActivityIntent = new Intent();
        Bundle b = new Bundle();
        b.putLong(ClazzListPresenter.ARG_CLAZZ_UID, testClazz.getClazzUid());
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);

        Matcher<View> logMatcher = allOf(
                withText(InstrumentationRegistry.getContext().getText(
                        R.string.log).toString().toUpperCase()),
                isDescendantOfA(withId(R.id.activity_class_detail_tablayout)));
        onView(logMatcher).perform(click());
    }

    public String getLogEntryStatus(int nPresent, int nAbsent){
        return nPresent + " " + InstrumentationRegistry.getContext().getText(R.string.present) +
            ", " + nAbsent + " " + InstrumentationRegistry.getContext().getText(R.string.absent);
    }

    public void assertLogEntryTitleIsDisplayed(String title){
        onView(allOf(withId(R.id.item_clazzlog_log_date),
                withText(containsString(title)))).check(matches(isDisplayed()));
    }

    public void assertLogEntryStatusIsDisplayed(String title, int nPresent, int nAbsent){
        onView(allOf(withId(R.id.item_clazzlog_log_status_text),
                hasSibling(withText(title)),
                withText(containsString(getLogEntryStatus(nPresent,nAbsent)))
        )).check(matches(isDisplayed()));
    }

    @Test
    public void givenAppLoads_whenClassLogListFragmentStarts_shouldShowLogs() {

        //Assert that the attendance logs match with Daos
        assertLogEntryTitleIsDisplayed(TEST_PRETTY_DATE1);
        assertLogEntryTitleIsDisplayed(TEST_PRETTY_DATE2);
        assertLogEntryTitleIsDisplayed(TEST_PRETTY_DATE3);
        assertLogEntryTitleIsDisplayed(TEST_PRETTY_DATE4);

        //Assert that values and assert tick mark for taken attendance
        assertLogEntryStatusIsDisplayed(TEST_PRETTY_DATE1, 0, 0);
        assertLogEntryStatusIsDisplayed(TEST_PRETTY_DATE2, 0, 0);
        assertLogEntryStatusIsDisplayed(TEST_PRETTY_DATE3, 0, 0);
        assertLogEntryStatusIsDisplayed(TEST_PRETTY_DATE4, 0, 0);

        //Assert their grouping by time as well.
        //TODO: Implement when done.

    }

    @Test
    public void givenLogsLoad_whenLogEntryClicked_shouldOpenItemAttendanceActivity(){

        onView(withId(R.id.fragment_class_log_list_recyclerview)).perform(
                RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(containsString(TEST_PRETTY_DATE1))), click()));

        /*
        Assert that un-taken attendance item is clickable to open ClassAttendanceActivity
        for that item
         */
        intended(allOf(hasComponent(ClazzLogDetailActivity.class.getCanonicalName())
                , hasExtras(allOf( hasEntry(equalTo(ClazzListPresenter.ARG_CLAZZ_UID),
                        equalTo(testClazz.getClazzUid())),
                        hasEntry(equalTo(ClazzLogDetailPresenter.ARG_LOGDATE),
                                equalTo(TEST_LOG_DATE1))))));

        //Assert nothing filled for this un-taken attendance
        onView(allOf(hasSibling(withText(TEST_CLASS_MEMBER1_NAME)),
                withId(R.id.item_clazzlog_detail_student_name))).check(matches(isDisplayed()));
        checkStudentEntryIsDisplayed(TEST_CLASS_MEMBER1_NAME);
        checkIconColorForStudent(TEST_CLASS_MEMBER1_NAME,
                R.id.item_clazzlog_detail_student_present_icon, R.color.color_gray);
        checkStudentEntryIsDisplayed(TEST_CLASS_MEMBER2_NAME);
        checkIconColorForStudent(TEST_CLASS_MEMBER1_NAME,
                R.id.item_clazzlog_detail_student_present_icon, R.color.color_gray);
        checkStudentEntryIsDisplayed(TEST_CLASS_MEMBER3_NAME);
        checkIconColorForStudent(TEST_CLASS_MEMBER1_NAME,
                R.id.item_clazzlog_detail_student_present_icon, R.color.color_gray);
        checkStudentEntryIsDisplayed(TEST_CLASS_MEMBER4_NAME);
        checkIconColorForStudent(TEST_CLASS_MEMBER1_NAME,
                R.id.item_clazzlog_detail_student_present_icon, Color.GRAY);


    }
    @Test
    public void givenLogsLoad_whenMarkedLogEntryClicked_shouldOpenItemAttendanceActivityWithValuesFilledIn(){
        //TOOD: this ?
    }

    @Test
    public void givenLogsLoad_whenRecordAttendanceClicked_shouldOpenNewAttendanceActivity(){

        onView(withId(R.id.fragment_class_log_record_attendance_fab)).perform(click());

        //TODO: implement when done
        /*
        long todayDate = UMCalendarUtil.getDateInMilliPlusDays(0);

        //Assert Class attendance activity opens up and fills students per class details DAO.
        intended(allOf(hasComponent(ClazzLogDetailActivity.class.getCanonicalName())
                , hasExtras(allOf( hasEntry(equalTo(ClazzListPresenter.ARG_CLAZZ_UID),
                        equalTo(testClazz.getClazzUid())),
                        hasEntry(equalTo(ClazzLogDetailPresenter.ARG_LOGDATE),
                                equalTo(todayDate))))));
        */

    }
}
