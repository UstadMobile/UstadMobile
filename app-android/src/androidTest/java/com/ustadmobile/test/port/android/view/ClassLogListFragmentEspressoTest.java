package com.ustadmobile.test.port.android.view;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ustadmobile.port.android.view.ClassDetailActivity;
import com.ustadmobile.port.android.view.ClassLogListFragment;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.AllOf.allOf;
import static android.support.test.espresso.assertion.ViewAssertions.matches;


/**
 * ClassLogListFragment's Espresso UI Test for Android
 */
@RunWith(AndroidJUnit4.class)
public class ClassLogListFragmentEspressoTest {

    /**
     * This sets the activity that we want floating around
     */
    @Rule
    public ActivityTestRule<ClassDetailActivity> baseActivityRule =
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
    public void givenAppLoads_whenClassLogListFragmentStarts_shouldShowLogs() {
        //TODO: Write tests

        //Assert that the attendance logs match with Daos
        //Assert that values and assert tick mark for taken attendance
        //Assert their grouping by time as well.

    }

    @Test
    public void givenLogsLoad_whenAttendanceItemClicked_shouldOpenItemAttendanceActivity(){
        //TODO: Write tests

        //Assert that untaken attendance item is clickable to open ClassAttendanceActivity
        // for that item
        //Assert that attendance taken item is clickable to open ClassAttendanceActivity
        // with values filled in
    }

    @Test
    public void givenLogsLoad_whenRecordAttendanceClicked_shouldOpenNewAttendanceActivity(){
        //TOOD: this

        //Assert Class attendance activity opens up and fills students per class details DAO.
    }
}
