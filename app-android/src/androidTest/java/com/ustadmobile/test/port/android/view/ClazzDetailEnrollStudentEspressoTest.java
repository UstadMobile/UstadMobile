package com.ustadmobile.test.port.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.port.android.view.ClazzDetailEnrollStudentActivity;
import com.ustadmobile.test.port.android.testutil.UmDbTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Hashtable;

import static com.ustadmobile.core.view.ClazzListView.ARG_CLAZZ_UID;

@RunWith(AndroidJUnit4.class)
public class ClazzDetailEnrollStudentEspressoTest {

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

    private Long personUid;


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
    public IntentsTestRule<ClazzDetailEnrollStudentActivity> mActivityRule =
            new IntentsTestRule<>(ClazzDetailEnrollStudentActivity.class, false, false);

    @Before
    public void beforeTest() {
        Context context = InstrumentationRegistry.getContext();

        //Clear the database
        UmAppDatabase.getInstance(context).clearAllTables();

        //Populate the database
        testClazz = UmDbTestUtil.createClazzWithClazzMembers(TEST_CLASS_NAME, TEST_CLASS_PERCENTAGE,
                peopleMap, TEST_USER_UID, context);

        Person testPerson = UmDbTestUtil.createPersonWithFieldsAndCustomFields(testClazz, context);
        personUid = testPerson.getPersonUid();

        //Start the activity
        Intent launchActivityIntent = new Intent();
        Bundle b = new Bundle();
        b.putLong(ARG_CLAZZ_UID, testClazz.getClazzUid());
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);

    }

    @Test
    public void givenActivityStarted_whenDataLoaded_shouldShowStudentListWithEnrollmentOptions() {
        Assert.assertTrue(true);

        //All students that are supposed to be there are there

        //Students that are supposed to be enrolled have enrolled checked

        //Students that are not enrolled (ie not part of the class) have Enrolled unchecked
    }

    @Test
    public void givenActivityStarted_whenDataLoaded_shouldShowStudentNotEnrolledUnchecked(){

        //Students that are not enrolled (ie not part of the class) have Enrolled unchecked.
    }

    @Test
    public void givenDataLoaded_whenEnrollStateChanged_shouldReflectInDao(){

        //Uncheck an already checked enrollment - check that its reflected in the Dao

        //Check an unchecked enrollment - check that its reflected in the Dao

    }

    @Test
    public void givenDataLoad_whenEnrollNewStudentClickedAndFilled_shouldEnrollNewStudentToClass(){

        //Click on button - verify new enrollment activity opened with blank fields.

        //Verify clazzUid sent to new enrollment activity.

    }

    @Test
    public void givenActivityStarted_whenDoneClicked_shouldCloseActivity(){

        //Verify that the activity has closed (not in display)
    }

}
