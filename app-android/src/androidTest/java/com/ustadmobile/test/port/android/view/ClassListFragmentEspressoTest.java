package com.ustadmobile.test.port.android.view;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ustadmobile.port.android.view.BasePointActivity2;


/**
 * ClazzListFragment's Espresso UI Test for Android
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
    public void givenAppLoads_whenClassListFragmentStarts_shouldShowClassesCards() {
        //TODO: Write tests

        //Assert the class card list recycler view populates according to DAO
        //Assert value in those cards matches with the classes themselves via DAO.


    }

    @Test
    public void givenClassesListCardLoads_whenItemRecordAttendanceButtonPressed_shouldLoadAttendanceActivity(){
        //TODO: this

        //Assert that a class, the record attendance button opens up the attendance activity for that
        //class with students populated and today's date.
    }

    @Test
    public void givenClassListFragmentStarted_whenClassClicked_shouldLaunchClassDetailActivity(){
        //TODO: this

        //Assert that when class cicked goes to its Class Detail activity page.
    }

}
