package com.ustadmobile.test.port.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.port.android.view.ClazzActivityEditActivity;
import com.ustadmobile.test.port.android.testutil.UmDbTestUtil;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Hashtable;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;
import static com.ustadmobile.core.view.ClazzListView.ARG_LOGDATE;


/**
 * ClazzActivityEditActivity's Espresso UI Test for Android
 */
@RunWith(AndroidJUnit4.class)
public class ClazzActivityEditActivityEspressoTest {

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
    private static Hashtable peopleMap;

    private Clazz testClazz;

    static {
        peopleMap = new Hashtable();
        peopleMap.put(TEST_CLASS_MEMBER1_NAME, TEST_CLASS_MEMBER1_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER2_NAME, TEST_CLASS_MEMBER2_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER3_NAME, TEST_CLASS_MEMBER3_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER4_NAME, TEST_CLASS_MEMBER4_PERCENTAGE);
    }

    /**
     * This sets the activity that we want floating around
     */
    @Rule
    public IntentsTestRule<ClazzActivityEditActivity> mActivityRule = new IntentsTestRule<>
            (ClazzActivityEditActivity.class, false, false);


    @Before
    public void beforeClass() {
        Context context = InstrumentationRegistry.getContext();
        UmAppDatabase.getInstance(context).clearAllTables();

        //Populate the database with test class:
        testClazz = UmDbTestUtil.createClazzWithClazzMembers(TEST_CLASS_NAME, TEST_CLASS_PERCENTAGE,
                peopleMap, TEST_USER_UID, context);

        UmDbTestUtil.addActivityChangeDetails(context);


        //Start the activity
        Intent launchActivityIntent = new Intent();

        Bundle b = new Bundle();
        b.putLong(ARG_CLAZZ_UID, testClazz.getClazzUid());
        b.putLong(ARG_LOGDATE, System.currentTimeMillis());

        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);

    }

    @Test
    public void givenAppLoads_whenClazzActivityEditActivityStarts_shouldLoadData() {
        //TODO: Write tests

        Assert.assertEquals(0,0);

        //Assert that the activity spinner / drop-down contains all of it.

        //Assert that clicking different activity changes results in different unit of measurement
        // types.

        //Assert that clicking thumbs up toggles between the good and bad image views

        //Assert that notes can be written

        //Assert that upon click Done, the activity closes.

    }

    @Test
    public void givenActivityStarts_whenFilledInClickedDone_shouldAddActivityChange(){

    }

    @Test
    public void givenActivityStarts_whenDateChanged_shouldLoadDataForAnother(){

    }

    @Test
    public void givenDataFilled_whenCancelledAndStartedAgain_shouldStartNew(){

    }
}
