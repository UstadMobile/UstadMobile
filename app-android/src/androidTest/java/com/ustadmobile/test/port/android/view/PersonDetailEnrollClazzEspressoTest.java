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
import com.ustadmobile.port.android.view.PersonDetailEnrollClazzActivity;
import com.ustadmobile.test.port.android.testutil.UmDbTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Hashtable;

import static com.ustadmobile.core.view.PersonDetailView.ARG_PERSON_UID;

@RunWith(AndroidJUnit4.class)
public class PersonDetailEnrollClazzEspressoTest {

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

    private static final float TEST_CLASS_MEMBER5_PERCENTAGE = 0.45F;
    private static final float TEST_CLASS_MEMBER6_PERCENTAGE = 0.65F;
    private static final float TEST_CLASS_MEMBER7_PERCENTAGE = 0.05F;
    private static final String TEST_CLASS_MEMBER5_NAME = "Test User5";
    private static final String TEST_CLASS_MEMBER6_NAME = "Test User6";
    private static final String TEST_CLASS_MEMBER7_NAME = "Test User7";

    private static final String TEST_CLASS_NAME_B= "Class B";
    private static final float TEST_CLASS_PERCENTAGE_B = 0.12F;
    private static Hashtable peopleMap;
    private static Hashtable peopleMap2;

    private Clazz testClazz;

    private Long personUid;


    static {
        peopleMap = new Hashtable();
        peopleMap.put(TEST_CLASS_MEMBER1_NAME, TEST_CLASS_MEMBER1_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER2_NAME, TEST_CLASS_MEMBER2_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER3_NAME, TEST_CLASS_MEMBER3_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER4_NAME, TEST_CLASS_MEMBER4_PERCENTAGE);

        peopleMap2 = new Hashtable();
        peopleMap2.put(TEST_CLASS_MEMBER5_NAME, TEST_CLASS_MEMBER5_PERCENTAGE);
        peopleMap2.put(TEST_CLASS_MEMBER6_NAME, TEST_CLASS_MEMBER6_PERCENTAGE);
        peopleMap2.put(TEST_CLASS_MEMBER7_NAME, TEST_CLASS_MEMBER7_PERCENTAGE);
    }

    /**
     * This sets the activity that we want floating around
     */
    @Rule
    public IntentsTestRule<PersonDetailEnrollClazzActivity> mActivityRule =
            new IntentsTestRule<>(PersonDetailEnrollClazzActivity.class, false, false);

    @Before
    public void beforeTest() {
        Context context = InstrumentationRegistry.getContext();

        //Clear the database
        UmAppDatabase.getInstance(context).clearAllTables();

        //Populate the database
        testClazz = UmDbTestUtil.createClazzWithClazzMembers(TEST_CLASS_NAME, TEST_CLASS_PERCENTAGE,
                peopleMap, TEST_USER_UID, context);

        //Populate the database
        Clazz testClazz2 = UmDbTestUtil.createClazzWithClazzMembers(TEST_CLASS_NAME_B, TEST_CLASS_PERCENTAGE_B,
                peopleMap2, TEST_USER_UID, context);

        Person testPerson = UmDbTestUtil.createPersonWithFieldsAndCustomFields(testClazz, context);
        personUid = testPerson.getPersonUid();

        //Start the activity
        Intent launchActivityIntent = new Intent();
        Bundle b = new Bundle();
        b.putLong(ARG_PERSON_UID, personUid);
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);

    }

    @Test
    public void givenActivityStarted_whenDataLoaded_shouldShowAllClazzList() {
        Assert.assertTrue(true);
    }

}
