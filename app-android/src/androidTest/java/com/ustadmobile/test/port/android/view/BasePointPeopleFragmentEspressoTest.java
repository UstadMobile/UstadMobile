package com.ustadmobile.test.port.android.view;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.port.android.view.BasePointActivity2;
import com.ustadmobile.port.android.view.BasePointPeopleFragment;
import com.ustadmobile.test.port.android.testutil.UmDbTestUtil;

import java.util.Hashtable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.ustadmobile.test.port.android.testutil.RecyclerViewChildAction.clickOnDescendantViewWithId;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;


/**
 * BasePointPeopleFragment's Espresso UI Test for Android
 */
@RunWith(AndroidJUnit4.class)
public class BasePointPeopleFragmentEspressoTest {

    private static final String TEST_CLASS_NAME = "Class A";
    private static final float TEST_CLASS_PERCENTAGE = 0.42F;
    private static final long TEST_USER_UID = 1L;
    private static final String TEST_CLASS_MEMBER1_NAME = "Test User1";
    private static final float TEST_CLASS_MEMBER1_PERCENTAGE = 0.21F;
    private static final float TEST_CLASS_MEMBER2_PERCENTAGE = 0.21F;
    private static final String TEST_CLASS_MEMBER2_NAME = "Test User2";
    private static Hashtable peopleMap;
    private Clazz testClazz;


    static {
        peopleMap = new Hashtable();
        peopleMap.put(TEST_CLASS_MEMBER1_NAME, TEST_CLASS_MEMBER1_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER2_NAME, TEST_CLASS_MEMBER2_PERCENTAGE);
    }

    /**
     * This sets the activity that we want floating around
     */
    @Rule
    public IntentsTestRule<BasePointActivity2> mActivityRule =
            new IntentsTestRule<>(BasePointActivity2.class, false, false);


    @Before
    public void beforeTest() throws Throwable {
        Context context = InstrumentationRegistry.getTargetContext();
        UmAppDatabase.getInstance(context).clearAllTables();

        testClazz = UmDbTestUtil.createClazzWithClazzMembers(TEST_CLASS_NAME, TEST_CLASS_PERCENTAGE,
                peopleMap, TEST_USER_UID, context);

        mActivityRule.launchActivity(new Intent());

        mActivityRule.runOnUiThread(() ->
                ((AHBottomNavigation)mActivityRule.getActivity().findViewById(R.id.bottom_navigation)
                ).setCurrentItem(1));
    }

    @Test
    public void givenAppLoads_whenBasePointPeopleFragmentStarts_shouldShowTabs() {
        //Assert Classes and People Tabs are loaded
        onView(allOf(
                withText(InstrumentationRegistry.getContext().getText(
                        R.string.classes).toString().toUpperCase()),
                isDescendantOfA(withId(R.id.fragment_base_point_people_tabs))))
                .check(matches(isDisplayed()));
        onView(allOf(
                withText(InstrumentationRegistry.getContext().getText(
                        R.string.people).toString().toUpperCase()),
                isDescendantOfA(withId(R.id.fragment_base_point_people_tabs))))
                .check(matches(isDisplayed()));

    }

    @Test
    public void givenBasePointPeopleFragmentLoads_whenClassesTabClicked_shouldGoToClassesListFragment(){

        Matcher<View> matcher = allOf(
                withText(InstrumentationRegistry.getContext().getText(
                        R.string.classes).toString().toUpperCase()),
                isDescendantOfA(withId(R.id.fragment_base_point_people_tabs)));
        onView(matcher).perform(click());

        //Assert classes fragment starts and loads
        onView(withId(R.id.fragment_class_list_recyclerview))
                .check(matches(isDisplayed()));

    }

    @Test
    public void givenBasePointPeopleFragmentLoads_whenPeopleTabClicked_shouldGoToPeopleListFragment() throws InterruptedException {
        Matcher<View> matcher = allOf(
                withText(InstrumentationRegistry.getContext().getText(
                        R.string.people).toString().toUpperCase()),
                isDescendantOfA(withId(R.id.fragment_base_point_people_tabs)));
        onView(matcher).perform(click());
        Thread.sleep(1000);

        //Assert people fragment starts and loads.
        onView(withId(R.id.fragment_people_list_recyclerview))
                .check(matches(isDisplayed()));

    }
}
