package com.ustadmobile.test.port.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzListPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.port.android.view.ClazzDetailActivity;
import com.ustadmobile.test.port.android.testutil.UmDbTestUtil;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Hashtable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;


/**
 * ClazzDetailActivity's Espresso UI Test for Android
 */
@RunWith(AndroidJUnit4.class)
public class ClazzDetailActivityEspressoTest {

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
        Context context = InstrumentationRegistry.getTargetContext();
        UmAppDatabase.getInstance(context).clearAllTables();

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
    public void givenAppLoads_whenClassDetailActivityStarts_shouldShowTabs() {
        //Assert that the Students, Log and Schedule Tabs show OK
        onView(allOf(
                withText(InstrumentationRegistry.getContext().getText(
                        R.string.students_literal).toString().toUpperCase()),
                isDescendantOfA(withId(R.id.activity_class_detail_tablayout))))
                .check(matches(isDisplayed()));
        onView(allOf(
                withText(InstrumentationRegistry.getContext().getText(
                        R.string.log).toString().toUpperCase()),
                isDescendantOfA(withId(R.id.activity_class_detail_tablayout))))
            .check(matches(isDisplayed()));

        onView(allOf(
                withText(InstrumentationRegistry.getContext().getText(
                        R.string.schedule).toString().toUpperCase()),
                isDescendantOfA(withId(R.id.activity_class_detail_tablayout))))
                .check(matches(isDisplayed()));

    }

    @Test
    public void givenClassDetailActivityLoads_whenStudnetTabClicked_shouldGoToFragment(){
        Matcher<View> matcher = allOf(
                withText(InstrumentationRegistry.getContext().getText(
                        R.string.students_literal).toString().toUpperCase()),
                isDescendantOfA(withId(R.id.activity_class_detail_tablayout)));
        onView(matcher).perform(click());

        //Assert that the tab click leads to the fragment load and title change
        onView(withId(R.id.fragment_class_student_list_recyclerview))
                .check(matches(isCompletelyDisplayed()));

    }

    @Test
    public void givenClassDetailActivityLoads_whenLogTabClicked_shouldGoToFragment(){
        Matcher<View> matcher = allOf(
                withText(InstrumentationRegistry.getContext().getText(
                        R.string.log).toString().toUpperCase()),
                isDescendantOfA(withId(R.id.activity_class_detail_tablayout)));
        onView(matcher).perform(click());

        //Assert that the tab click leads to the fragment load and title change
        onView(withId(R.id.fragment_class_log_list_recyclerview))
                .check(matches(isCompletelyDisplayed()));
    }

    @Test
    public void givenClassDetailActivityLoads_whenScheduleTabClicked_shouldGoToFragment(){
        Matcher<View> matcher = allOf(
                withText(InstrumentationRegistry.getContext().getText(
                        R.string.schedule).toString().toUpperCase()),
                isDescendantOfA(withId(R.id.activity_class_detail_tablayout)));
        onView(matcher).perform(click());

        //Assert that the tab click leads to the fragment load and title change

    }

    @After
    public void afterTest(){

    }

}
