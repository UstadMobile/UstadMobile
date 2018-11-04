package com.ustadmobile.test.port.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.core.view.PersonDetailView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.port.android.view.PersonEditActivity;
import com.ustadmobile.test.port.android.testutil.UmDbTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Hashtable;

@RunWith(AndroidJUnit4.class)
public class PersonEditActivityEspressoTest {

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
    public IntentsTestRule<PersonEditActivity> mActivityRule =
        new IntentsTestRule<>(PersonEditActivity.class, false, false);

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
        b.putLong(ClazzListView.ARG_CLAZZ_UID, testClazz.getClazzUid());
        b.putLong(PersonDetailView.ARG_PERSON_UID, personUid);
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);

    }

    @Test
    public void givenActivityStarted_whenUserDetailsLoaded_shouldShowPersonFieldsAndCustomFields() {
        Assert.assertTrue(true);

        //Should show all core fields with values filled in
        //Assert the type of field with field type
        //Assert icon
        //Should fill values in

        //Should show all custom fields with values filled in.
        //Assert the type of field with field type (custom - always string)
        //Assert icon


    }

    @Test
    public void givenActivityStarted_whenAddPersonToClassClicked_shouldOpenClassEnrollmentActivity(){

        //Click Add Person To Class button - assert it goes to new activity.
        //Assert person Uid is sent as argument to new activity.

    }

    @Test
    public void givenActivityStarted_whenFieldEditedAndDonePressed_shouldPersistPerson(){

        //Fill Values core fields - click done - assert update went up to person.
        //Fill values in custom field - click done - assert update went to person's custom field.

    }

    @Test
    public void givenActivityStarted_whenFieldEdited_shouldNotPersist(){

        //Fill core field values - assert update did NOT Persist to Person
        //Fill custom field values - assert update did NOT Persist to Person.

    }

    @Test
    public void givenActivityStarted_changeDate_shouldUpdateDate(){

        //Edit Birthday - pick custom date - assert date field updated with pretty text.

        //Click Done - assert new updated date persisted to Person

    }

    @Test
    public void givenActivityStarted_changeNumberAndClickDone_shouldUpdateNumber(){

        //Edit Phone number for Mother - pick number - click done - assert value persisted to Person
    }


}



