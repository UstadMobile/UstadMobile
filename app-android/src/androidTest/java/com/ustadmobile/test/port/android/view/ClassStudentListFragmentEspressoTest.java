package com.ustadmobile.test.port.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzListPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.port.android.view.ClazzDetailActivity;
import com.ustadmobile.test.port.android.testutil.CustomMatcherFilters;
import com.ustadmobile.test.port.android.testutil.UmDbTestUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Hashtable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.AllOf.allOf;


/**
 * ClazzStudentListFragment's Espresso UI Test for Android
 */
@RunWith(AndroidJUnit4.class)
public class ClassStudentListFragmentEspressoTest {

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
    public IntentsTestRule<ClazzDetailActivity> mActivityRule =
            new IntentsTestRule<>(ClazzDetailActivity.class, false, false);

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
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);

    }

    @Test
    public void givenAppLoads_whenClassStudentListFragmentStarts_shouldShowStudents() {

        String attendancePercentage = (int)(TEST_CLASS_MEMBER1_PERCENTAGE * 100) + "%";

        //Assert students load in recycler view.
        onView(allOf(withId(R.id.item_clazzstudentlist_student_title),
                withText(TEST_CLASS_MEMBER1_NAME)))
                .check(matches(isDisplayed()));

        //Assert attendance numbers with DAOs
        onView(allOf(hasSibling(withText(TEST_CLASS_MEMBER1_NAME)),
                withId(R.id.item_clazzstudentlist_student_attendance_percentage),
                withText(containsString(attendancePercentage)))).check(matches(isDisplayed()));

        //Assert attendance numbers and their colors match
        onView( allOf(hasSibling(withText(TEST_CLASS_MEMBER4_NAME)),
                withId(R.id.item_clazzstudentlist_attendance_trafficlight)))
            .check(matches(CustomMatcherFilters.withColorFilter(R.color.traffic_green))
            );

        onView( allOf(hasSibling(withText(TEST_CLASS_MEMBER3_NAME)),
                withId(R.id.item_clazzstudentlist_attendance_trafficlight)))
            .check(matches(CustomMatcherFilters.withColorFilter(R.color.traffic_orange))
            );

        onView( allOf(hasSibling(withText(TEST_CLASS_MEMBER2_NAME)),
                withId(R.id.item_clazzstudentlist_attendance_trafficlight)))
                .check(matches(CustomMatcherFilters.withColorFilter(R.color.traffic_red))
                );

        //Assert student size and each match with Daos


    }



}
