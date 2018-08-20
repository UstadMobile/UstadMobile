package com.ustadmobile.test.port.android.view;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ustadmobile.port.android.view.BasePointActivity2;
import com.ustadmobile.port.android.view.ClassListFragment;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.AllOf.allOf;
import static android.support.test.espresso.assertion.ViewAssertions.matches;


/**
 * ClassListFragment's Espresso UI Test for Android
 */
@RunWith(AndroidJUnit4.class)
public class ClassListFragmentEspressoTest {

    /**
     * This sets the activity that we want floating around
     */
    @Rule
    public ActivityTestRule<BasePointActivity2> baseActivityRule =
            new ActivityTestRule<>(BasePointActivity2.class);

    @BeforeClass
    public static void beforeClass() {
        //Before class stuff here
    }

    @Before
    public void beforeTest() {
        //Before here..
    }

    @Test
    public void givenAppLoads_whenClassListFragmentStarts_shouldLoadClassesCards() {
        //TODO: Write tests

        //Assert the class card list recycler view populates according to DAO
        //Assert value in those cards matches with the classes themseves via DAO.
        //Assert attendance color matches with all cases.

    }

    @Test
    public void givenClassesListCardLoads_whenItemRecordAttendanceButtonPressed_shouldLoadAttendanceActivity(){
        //TODO: this

        //Assert that a class, the record attendance button opens up the attendance activity for that
        //class with students populated and today's date.
    }

}
