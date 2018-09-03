package com.ustadmobile.test.port.android.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzListPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.port.android.view.BasePointActivity2;
import com.ustadmobile.port.android.view.ClassDetailActivity;
import com.ustadmobile.port.android.view.ClassLogDetailActivity;
import com.ustadmobile.test.port.android.testutil.ActivityStopCountdownLatch;
import com.ustadmobile.test.port.android.testutil.UmDbTestUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.ustadmobile.test.port.android.testutil.RecyclerViewChildAction.clickOnDescendantViewWithId;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;


/**
 * ClazzListFragment's Espresso UI Test for Android
 */
@RunWith(AndroidJUnit4.class)
public class ClassListFragmentEspressoTest {


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
    public void beforeTest() throws Throwable{
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
    public void givenAppLoads_whenClassListFragmentStarts_shouldShowClassesCards() {

        String attendancePercentage = (int)(TEST_CLASS_PERCENTAGE * 100) + "%";

        onView(allOf(withId(R.id.item_clazzlist_clazz_title), withText(TEST_CLASS_NAME)))
                .check(matches(isDisplayed()));
        onView(allOf(hasSibling(withText(TEST_CLASS_NAME)),
                withId(R.id.item_clazzlist_attendance_percentage),
                withText(containsString(attendancePercentage)))).check(matches(isDisplayed()));
    }

    @Test
    public void givenClassesListCardLoads_whenItemRecordAttendanceButtonPressed_shouldLoadAttendanceActivity(){

        ActivityStopCountdownLatch latch = new ActivityStopCountdownLatch(mActivityRule.getActivity());

        onView(withId(R.id.fragment_class_list_recyclerview)).perform(
                RecyclerViewActions.actionOnItem(hasDescendant(withText(TEST_CLASS_NAME)),
                        clickOnDescendantViewWithId(R.id.item_clazzlist_attendance_record_attendance_button)));

        latch.wait(10, TimeUnit.SECONDS);

        intended(allOf(
                hasComponent(ClassLogDetailActivity.class.getCanonicalName()),
                hasExtra(equalTo(ClazzListPresenter.ARG_CLAZZ_UID),
                        equalTo(testClazz.getClazzUid()))
        ));
    }

    @Test
    public void givenClassListFragmentStarted_whenClassClicked_shouldLaunchClassDetailActivity(){
        ActivityStopCountdownLatch latch = new ActivityStopCountdownLatch(mActivityRule.getActivity());

        onView(withId(R.id.fragment_class_list_recyclerview)).perform(
                RecyclerViewActions.actionOnItem(hasDescendant(withText(TEST_CLASS_NAME)), click()));


        latch.wait(10, TimeUnit.SECONDS);

        intended(allOf(
                hasComponent(new ComponentName(InstrumentationRegistry.getTargetContext(),
                        ClassDetailActivity.class)),
                hasExtra(equalTo(ClazzListPresenter.ARG_CLAZZ_UID),
                        equalTo(testClazz.getClazzUid()))
        ));

    }

}
