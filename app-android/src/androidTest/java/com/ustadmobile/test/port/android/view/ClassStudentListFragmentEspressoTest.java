package com.ustadmobile.test.port.android.view;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ustadmobile.port.android.view.ClassDetailActivity;

import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;


/**
 * ClazzStudentListFragment's Espresso UI Test for Android
 */
@RunWith(AndroidJUnit4.class)
public class ClassStudentListFragmentEspressoTest {

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
    public void givenAppLoads_whenClassStudentListFragmentStarts_shouldShowStudents() {
        //TODO: Write tests

        //Assert students load in recycler view.
        //Assert student size and each match with Daos
        //Assert attendance numbers with DAOs
        //Assert attendance numbers and their colors match

    }

}
