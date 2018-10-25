package com.ustadmobile.test.port.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzListPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.port.android.view.ClazzLogDetailActivity;
import com.ustadmobile.test.port.android.testutil.CustomMatcherFilters;
import com.ustadmobile.test.port.android.testutil.UmDbTestUtil;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Hashtable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.ustadmobile.test.port.android.testutil.RecyclerViewChildAction.clickOnDescendantViewWithId;
import static org.hamcrest.core.AllOf.allOf;

/**
 * ClassAttendanceActivity's Espresso UI Test for Android
 */
@RunWith(AndroidJUnit4.class)
public class ClazzLogDetailActivityActivityEspressoTest {

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
    private static Hashtable peopleMap;

    private Clazz testClazz;


    static {
        peopleMap = new Hashtable();
        peopleMap.put(TEST_CLASS_MEMBER1_NAME, TEST_CLASS_MEMBER1_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER2_NAME, TEST_CLASS_MEMBER2_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER3_NAME, TEST_CLASS_MEMBER3_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER4_NAME, TEST_CLASS_MEMBER4_PERCENTAGE);
    }

    /**
     * This sets the activity that we want floating around
     */
    @Rule
    public IntentsTestRule<ClazzLogDetailActivity> mActivityRule =
            new IntentsTestRule<>(ClazzLogDetailActivity.class, false, false);

    @Before
    public void beforeTest() {
        Context context = InstrumentationRegistry.getContext();
        UmAppDatabase.getInstance(context).clearAllTables();

        //Populate the database
        testClazz = UmDbTestUtil.createClazzWithClazzMembers(TEST_CLASS_NAME, TEST_CLASS_PERCENTAGE,
                peopleMap, TEST_USER_UID, context);

        //Start the activity
        Intent launchActivityIntent = new Intent();
        Bundle b = new Bundle();
        b.putLong(ClazzListPresenter.ARG_CLAZZ_UID, testClazz.getClazzUid());
        b.putLong(ClazzListView.ARG_LOGDATE, UMCalendarUtil.getDateInMilliPlusDays(0));
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);

    }

    public static void checkStudentEntryIsDisplayed(String studentName){
        onView(allOf(hasSibling(withText(studentName)),
                withId(R.id.item_clazzlog_detail_student_name))).check(matches(isDisplayed()));
    }

    public void clickOnStudentEntryAttendanceButton(String studentName, int attendanceIcon){
        onView(withId(R.id.class_log_detail_container_recyclerview)).perform(
                RecyclerViewActions.actionOnItem(hasDescendant(withText(studentName)),
                        clickOnDescendantViewWithId(attendanceIcon)));
    }

    public static void checkIconColorForStudent(String studentName, int attendanceIcon, int expectedColor){
        onView( allOf(hasSibling(withText(studentName)),
                withId(attendanceIcon)))
                .check(matches(CustomMatcherFilters.withColorFilter(expectedColor))
                );
    }

    @Test
    public void givenActivityStarted_whenUserTapsStudentPresent_shouldTintPresentIcon() {

        checkStudentEntryIsDisplayed(TEST_CLASS_MEMBER1_NAME);

        clickOnStudentEntryAttendanceButton(TEST_CLASS_MEMBER1_NAME,
                R.id.item_clazzlog_detail_student_present_icon);

        checkIconColorForStudent(TEST_CLASS_MEMBER1_NAME,
                R.id.item_clazzlog_detail_student_present_icon, R.color.traffic_green);

    }

    @Test
    public void givenActivityStarted_whenUserTapsStudentAbsent_shouldTintAbsentIcon() {

        checkStudentEntryIsDisplayed(TEST_CLASS_MEMBER2_NAME);

        clickOnStudentEntryAttendanceButton(TEST_CLASS_MEMBER2_NAME,
                R.id.item_clazzlog_detail_student_absent_icon);

        checkIconColorForStudent(TEST_CLASS_MEMBER2_NAME,
                R.id.item_clazzlog_detail_student_absent_icon, R.color.traffic_red);
    }

    @Test
    public void givenActivityStarted_whenUserTapsStudentPartial_shouldTintPartialIcon() {

        checkStudentEntryIsDisplayed(TEST_CLASS_MEMBER3_NAME);

        clickOnStudentEntryAttendanceButton(TEST_CLASS_MEMBER3_NAME,
                R.id.item_clazzlog_detail_student_delay_icon);

        checkIconColorForStudent(TEST_CLASS_MEMBER3_NAME,
                R.id.item_clazzlog_detail_student_delay_icon, R.color.traffic_orange);
    }

    @Test
    public void givenAttendanceActivityStart_whenUserRecordsAttendanceAndClicksDone_shouldSaveToDatabaseAndFinish(){
        //TODO: Finish

        clickOnStudentEntryAttendanceButton(TEST_CLASS_MEMBER1_NAME,
                R.id.item_clazzlog_detail_student_delay_icon);
        clickOnStudentEntryAttendanceButton(TEST_CLASS_MEMBER2_NAME,
                R.id.item_clazzlog_detail_student_present_icon);
        clickOnStudentEntryAttendanceButton(TEST_CLASS_MEMBER3_NAME,
                R.id.item_clazzlog_detail_student_present_icon);
        clickOnStudentEntryAttendanceButton(TEST_CLASS_MEMBER4_NAME,
                R.id.item_clazzlog_detail_student_absent_icon);
        clickOnStudentEntryAttendanceButton(TEST_CLASS_MEMBER4_NAME,
                R.id.item_clazzlog_detail_student_present_icon);
        clickOnStudentEntryAttendanceButton(TEST_CLASS_MEMBER4_NAME,
                R.id.item_clazzlog_detail_student_delay_icon);
        clickOnStudentEntryAttendanceButton(TEST_CLASS_MEMBER4_NAME,
                R.id.item_clazzlog_detail_student_absent_icon);

        onView(withId(R.id.class_log_detail__done_fab)).perform(click());


        //Assert Click Done closes the activity.
        Assert.assertTrue(mActivityRule.getActivity().isFinishing());

        //Assert attendance updated in the DAO.

        //Assert outstanding attendance is gone from the Feed

    }

    @Test
    public void givenAttendanceActivityStartedForPreviousRecord_whenResultsUpdated_shouldUpdateDatabase(){
        //TODO: this

        //Assert attendane open for already taken.
        //Assert details filled in as per current attendance entry in Dao
        //Change details and click done assert by opening same attendance for class+date again.
        //
    }

    @Test
    public void givenAttendanceActivityStarted_whenDateTabSwiped_shouldShowRecordsFromPreviousDate(){
        //TODO this

        //Assert can swipe towards left to show previous day's attendance.
        //Assert show previous dates attendance details if taken or not.
        //Assert can swipe towards right to show next day's attendance if current page is
        // at most yesterday's date
        //Assert can't swipe future date's attendance.
        //Assert can take attendance for previous date. Check via DAOs.
    }
}
