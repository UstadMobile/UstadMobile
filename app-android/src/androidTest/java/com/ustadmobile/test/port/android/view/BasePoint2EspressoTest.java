package com.ustadmobile.test.port.android.view;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.toughra.ustadmobile.R;
import com.ustadmobile.port.android.view.BasePointActivity2;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Android Espresso test that tests BasePoint2Activity
 */
@RunWith(AndroidJUnit4.class)
public class BasePoint2EspressoTest {

    @Rule
    public ActivityTestRule<BasePointActivity2> mActivityRule =
            new ActivityTestRule<>(BasePointActivity2.class);


    @Before
    public void beforeTest(){
        //Before here..
    }

    public void setBottomNavigationCurrentItem(int position) throws Throwable {

        //Lambda way
        mActivityRule.runOnUiThread(() ->
            ((AHBottomNavigation)mActivityRule.getActivity().findViewById(R.id.bottom_navigation)
                ).setCurrentItem(position));
    }

    public void changePeopleView(int position) throws Throwable {

        //Lambda way:
        mActivityRule.runOnUiThread(() ->
                ((ViewPager)
                    mActivityRule.getActivity().findViewById(R.id.fragment_base_point_people_container)
                ).setCurrentItem(position));
    }

    @Test
    public void givenAppLoads_whenBasePoint2Starts_shouldShowBottomNavigation(){

        onView(withId(R.id.base_point_2_coordinator)).check(matches(isDisplayed()));
        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()));

    }

    @Test
    public void givenBasePointLoads_whenFeedClicked_shouldGoToFeed() throws Throwable {
        setBottomNavigationCurrentItem(0);
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.base_point_2_toolbar))))
                .check(matches(withText(R.string.feed)));
        onView(withId(R.id.fragment_feed_list_recyclerview)).check(matches(isCompletelyDisplayed()));

    }

    @Test
    public void givenBasePointLoads_whenPeopleClicked_shouldGoToPeople() throws Throwable {
        setBottomNavigationCurrentItem(1);

        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.base_point_2_toolbar))))
                .check(matches(withText(R.string.my_classes)));
        onView(withId(R.id.fragment_base_point_people_container)).check(matches(isDisplayed()));
    }


}
