package com.ustadmobile.test.port.android.view;

import android.os.SystemClock;
import android.support.design.widget.TabLayout;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.toughra.ustadmobile.R;
import com.ustadmobile.port.android.view.BasePointActivity2;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.AllOf.allOf;
import static android.support.test.espresso.assertion.ViewAssertions.matches;




@RunWith(AndroidJUnit4.class)
public class BasePoint2EspressoTest {

    @Rule
    public ActivityTestRule<BasePointActivity2> mActivityRule =
            new ActivityTestRule<>(BasePointActivity2.class);

    @BeforeClass
    public static void beforeClass(){
        //Before class stuff here
    }

    @Before
    public void beforeTest(){
        //Before here..
    }

    public void changeNvigationView(int position){
        //Get the bottom navigation component.
        AHBottomNavigation bottomNavigation =
            mActivityRule.getActivity().findViewById(R.id.bottom_navigation);

        final Runnable changeView = new Runnable()
        {
            public void run()
            {
                bottomNavigation.setCurrentItem(position);
            }
        };

        try {
            mActivityRule.runOnUiThread(changeView);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void changePeopleView(int position){
        //Get the BasePointPeople TabLayout component
        ViewPager basepointPeopleFragmentContainer = (ViewPager)
                mActivityRule.getActivity().findViewById(R.id.fragment_base_point_people_container);
        final Runnable changeView = new Runnable() {
            @Override
            public void run() {
                basepointPeopleFragmentContainer.setCurrentItem(position);
            }
        };

        try {
            mActivityRule.runOnUiThread(changeView);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Test
    public void givenAppLoads_whenBasePoint2Starts_shouldShowBottomNavigation(){

        onView(withId(R.id.base_point_2_coordinator)).check(matches(isDisplayed()));
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()));

    }

    @Test
    public void givenAppLoads_whenBasePoint2Starts_shouldShowFeedFirst(){
        //TODO: Check this might not work since the test will not run the first time. We don't
        // know the order of things. Neither we know the state of the UI with other tests
        // being run before this. The current view is guaranteed not to be Feed.

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.base_point_2_toolbar))))
                .check(matches(withText(R.string.feed)));

    }

    @Test
    public void givenBasePointLoads_whenFeedClicked_shouldGoToFeed(){
        changeNvigationView(0);
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.base_point_2_toolbar))))
                .check(matches(withText(R.string.feed)));

    }

    @Test
    public void givenBasePointLoads_whenPeopleClicked_shouldGoToPeople(){
        changeNvigationView(1);
        SystemClock.sleep(1000);
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.base_point_2_toolbar))))
                .check(matches(withText(R.string.my_classes)));
        //Check if PeopleListFragment opened.
    }

    @Test
    public void givenBasePointLoads_whenStatisticsClicked_shouldGoToStatistics(){
        changeNvigationView(2);
        SystemClock.sleep(1000);
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.base_point_2_toolbar))))
                .check(matches(withText(R.string.statistcs)));
    }

    @Test
    public void givenBasePointLoads_whenLessonsClicked_shouldGoToLessons(){
        changeNvigationView(3);
        SystemClock.sleep(1000);
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.base_point_2_toolbar))))
                .check(matches(withText(R.string.lessons)));

    }

    @Test
    public void givenBasePointLoads_whenUserIconClicked_shouldGoToUserPanel(){
        //TODO: this

    }

}
