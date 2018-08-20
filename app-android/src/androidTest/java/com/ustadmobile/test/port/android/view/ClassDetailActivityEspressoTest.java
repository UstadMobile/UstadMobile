package com.ustadmobile.test.port.android.view;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ustadmobile.port.android.view.ClassDetailActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.AllOf.allOf;
import static android.support.test.espresso.assertion.ViewAssertions.matches;


/**
 * ClassDetailActivity's Espresso UI Test for Android
 */
@RunWith(AndroidJUnit4.class)
public class ClassDetailActivityEspressoTest {

    /**
     * This sets the activity that we want floating around
     */
    @Rule
    public ActivityTestRule<ClassDetailActivity> mActivityRule =
            new ActivityTestRule<>(ClassDetailActivity.class);

    @BeforeClass
    public static void beforeClass() {
        //Before class stuff here
    }

    @Before
    public void beforeTest() {
        //Before here..
    }

    @Test
    public void givenAppLoads_whenClassDetailActivityStarts_shouldShowTabs() {
        //TODO: Write tests

        //Assert that the Students, Log and Schedule Tabs show OK

    }

    @Test
    public void givenClassDetailActivityLoads_whenStudnetTabClicked_shouldGoToFragment(){
        //TODO: this

        //Assert that the tab click leads to the fragment load and title change

    }

    @Test
    public void givenClassDetailActivityLoads_whenLogTabClicked_shouldGoToFragment(){
        //TODO: this

        //Assert that the tab click leads to the fragment load and title change
    }

    @Test
    public void givenClassDetailActivityLoads_whenScheduleTabClicked_shouldGoToFragment(){
        //TODO: this

        //Assert that the tab click leads to the fragment load and title change
    }

}
