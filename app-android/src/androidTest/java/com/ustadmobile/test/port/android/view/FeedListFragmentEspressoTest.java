package com.ustadmobile.test.port.android.view;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzListPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.port.android.view.BasePointActivity2;
import com.ustadmobile.port.android.view.ClazzLogDetailActivity;
import com.ustadmobile.test.port.android.testutil.UmDbTestUtil;

import java.util.Hashtable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtras;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.matcher.BundleMatchers.hasEntry;


/**
 * FeedListFragment's Espresso UI Test for Android
 */
@RunWith(AndroidJUnit4.class)
public class FeedListFragmentEspressoTest {

    private static final String TEST_CLASS_NAME = "Class A";
    private static final float TEST_CLASS_PERCENTAGE = 0.42F;
    private static final long TEST_USER_UID = 1L;
    private static final String TEST_CLASS_MEMBER1_NAME = "Test User1";
    private static final float TEST_CLASS_MEMBER1_PERCENTAGE = 0.15F;
    private static final float TEST_CLASS_MEMBER2_PERCENTAGE = 0.35F;
    private static final float TEST_CLASS_MEMBER3_PERCENTAGE = 0.55F;
    private static final float TEST_CLASS_MEMBER4_PERCENTAGE = 0.85F;
    private static final String TEST_CLASS_MEMBER2_NAME = "Test User2";
    private static final String TEST_CLASS_MEMBER3_NAME = "Test User3";
    private static final String TEST_CLASS_MEMBER4_NAME = "Test User4";
    public static final String TEST_FEED_DESCRIPTION = "This is a test feed. Created from the tests.";
    public static final String TEST_FEED1_TITLE = "Test feed 1 notification.";
    public static final String TEST_FEED2_TITLE = "Test feed 2 notification.";
    public static Long TEST_FEED1_LOGDATE = UMCalendarUtil.getDateInMilliPlusDays(1);
    public static Long TEST_FEED2_LOGDATE = UMCalendarUtil.getDateInMilliPlusDays(-1);

    private static Hashtable peopleMap;
    private static Hashtable feedsToCreate;

    private Clazz testClazz;

    static {
        peopleMap = new Hashtable();
        peopleMap.put(TEST_CLASS_MEMBER1_NAME, TEST_CLASS_MEMBER1_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER2_NAME, TEST_CLASS_MEMBER2_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER3_NAME, TEST_CLASS_MEMBER3_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER4_NAME, TEST_CLASS_MEMBER4_PERCENTAGE);

        feedsToCreate = new Hashtable();
        feedsToCreate.put(TEST_FEED1_TITLE, TEST_FEED1_LOGDATE);
        feedsToCreate.put(TEST_FEED2_TITLE, TEST_FEED2_LOGDATE);

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

        UmDbTestUtil.createFeedEntries(feedsToCreate, testClazz.getClazzUid(), TEST_USER_UID, context);

        mActivityRule.launchActivity(new Intent());

        mActivityRule.runOnUiThread(() ->
                ((AHBottomNavigation)mActivityRule.getActivity().findViewById(R.id.bottom_navigation)
                ).setCurrentItem(0));
    }

    @Test
    public void givenAppStarts_whenFeedListLoads_shouldShowAndMatchFeedEntries() {

        //Assert feed entries match with DAO entries
        onView(allOf(withId(R.id.item_feedlist_feed_title),
                withText(containsString(TEST_FEED1_TITLE)))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.item_feedlist_feed_title),
                withText(containsString(TEST_FEED2_TITLE)))).check(matches(isDisplayed()));

    }

    @Test
    public void givenFeedListLoads_whenAttendanceItemClicked_shouldGoToAttendanceActivity(){

        //Assert can click today's and previous day's attendance feed item which goes to attendance
        onView(withId(R.id.fragment_feed_list_recyclerview)).perform(
                RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(containsString(TEST_FEED1_TITLE))), click()));
        intended(allOf(hasComponent(ClazzLogDetailActivity.class.getCanonicalName())
                , hasExtras(allOf( hasEntry(equalTo(ClazzListPresenter.ARG_CLAZZ_UID),
                    equalTo(Long.toString(testClazz.getClazzUid()))),
                        hasEntry(equalTo(ClazzListView.ARG_LOGDATE),
                                equalTo(Long.toString(TEST_FEED1_LOGDATE)))))));

    }

    @Test
    public void givenFeedListFragmentStarts_whenFeedListLoads_shouldUpdateFeedBadge() {
        //TODO: this when implemented

        //Assert load feedlist and the number of outstanding items matches with the feed button's
        // number notification icon (badge) in the bottom navigation of BasePoint2Activity

    }

}
