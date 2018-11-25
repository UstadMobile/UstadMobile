package com.ustadmobile.test.port.android.view;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.port.android.view.ClazzActivityListFragment;
import com.ustadmobile.port.android.view.ClazzDetailActivity;
import com.ustadmobile.test.port.android.testutil.UmDbTestUtil;

import com.ustadmobile.port.android.view.ClazzActivityListFragment;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.AllOf.allOf;
import static android.support.test.espresso.assertion.ViewAssertions.matches;


/**
 * ClazzActivityListFragment's Espresso UI Test for Android
 */
@RunWith(AndroidJUnit4.class)
public class ClazzActivityListFragmentEspressoTest {

    /**
     * This sets the activity that we want floating around
     */
    @Rule
    public IntentsTestRule<ClazzDetailActivity> mActivityRule = new IntentsTestRule<>
            (ClazzDetailActivity.class, false, false);

    @BeforeClass
    public static void beforeClass() {
        //Before class stuff here
        Context context = InstrumentationRegistry.getContext();
        UmAppDatabase.getInstance(context).clearAllTables();

        UmDbTestUtil.createDummyData(context);
    }

    @Before
    public void beforeTest() {
        //Before here..
    }

    @Test
    public void givenAppLoads_whenClazzActivityListFragmentStarts_should() {
        //TODO: Write tests 

    }

}
