package com.ustadmobile.test.port.android.view;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzListPresenter;
import com.ustadmobile.core.controller.ClazzLogDetailPresenter;
import com.ustadmobile.port.android.view.ClassDetailActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.AllOf.allOf;
import static android.support.test.espresso.assertion.ViewAssertions.matches;


/**
 * ClassDetailActivity's Espresso UI Test for Android
 */
@RunWith(AndroidJUnit4.class)
public class ClazzLogDetailActivityEspressoTest {

    /**
     * This sets the activity that we want floating around
     */
    @Rule
    public ActivityTestRule<ClassDetailActivity> mActivityRule =
            new ActivityTestRule<>(ClassDetailActivity.class, false, false);

    @BeforeClass
    public static void beforeClass() {
        //Before class stuff here
    }

    @Before
    public void beforeTest() {
        Intent startIntent = new Intent();
        startIntent.putExtra(ClazzListPresenter.ARG_CLAZZ_UID, 2);
        mActivityRule.launchActivity(startIntent);
        //Before here..
    }

    @After
    public void afterTest(){

    }

    public void givenActivityStarted_whenUserTapsStudentPresent_shouldTintPresentIcon() {

//        onView(allOf(hasSibling(withText("Student Name")),
//                withId(R.id.item_clazzlog_detail_student_present_icon))).perform(click());
//
//        //Assert that the Student clicked
//        onView(allOf(hasSibling(withText("Student Name")),
//                withId(R.id.item_clazzlog_detail_student_present_icon)))
//                    .check(matches(withContentDescription(R.string.present_selected)));


    }

//    @Test
//    public void givenAppLoads_whenClassDetailActivityStarts_shouldShowTabs() {
//
//
//        //TODO: Write tests
//
//        //Assert that the Students, Log and Schedule Tabs show OK
//
//    }
//
//    @Test
//    public void givenClassDetailActivityLoads_whenStudnetTabClicked_shouldGoToFragment(){
//        //TODO: this
//
//        //Assert that the tab click leads to the fragment load and title change
//
//    }
//
//    @Test
//    public void givenClassDetailActivityLoads_whenLogTabClicked_shouldGoToFragment(){
//        //TODO: this
//
//        //Assert that the tab click leads to the fragment load and title change
//    }
//
//    @Test
//    public void givenClassDetailActivityLoads_whenScheduleTabClicked_shouldGoToFragment(){
//        //TODO: this
//
//        //Assert that the tab click leads to the fragment load and title change
//    }

}
