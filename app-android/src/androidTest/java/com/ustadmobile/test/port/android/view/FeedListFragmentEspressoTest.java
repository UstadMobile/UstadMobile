package com.ustadmobile.test.port.android.view;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ustadmobile.port.android.view.BasePointActivity2;
import com.ustadmobile.port.android.view.FeedListFragment;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.AllOf.allOf;
import static android.support.test.espresso.assertion.ViewAssertions.matches;


/**
 * FeedListFragment's Espresso UI Test for Android
 */
@RunWith(AndroidJUnit4.class)
public class FeedListFragmentEspressoTest {

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
    public void givenAppStarts_whenFeedListLoads_shouldShowAndMatchFeedEntries() {
        //TODO: Write tests

        //Assert feed entries match with DAO entries

    }

    @Test
    public void givenFeedListLoads_whenAttendanceItemClicked_shouldGoToAttendanceActivity(){
        //TODO: this

        //Assert can click today's and previous day's attendance feed item which goes to attendance
    }

    @Test
    public void givenFeedListFragmentStarts_whenFeedListLoads_shouldUpdateFeedBadge() {
        //TODO: this

        //Assert load feedlist and the number of outstanding items matches with the feed button's
        // number notification icon (badge) in the bottom navigation of BasePoint2Activity

    }

}
