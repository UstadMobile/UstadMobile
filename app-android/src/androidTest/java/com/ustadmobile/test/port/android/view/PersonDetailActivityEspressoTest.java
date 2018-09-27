package com.ustadmobile.test.port.android.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzListPresenter;
import com.ustadmobile.core.controller.ClazzLogDetailPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.PersonDetailView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.port.android.view.ClazzLogDetailActivity;
import com.ustadmobile.port.android.view.PersonDetailActivity;
import com.ustadmobile.test.port.android.testutil.UmDbTestUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Hashtable;

@RunWith(AndroidJUnit4.class)
public class PersonDetailActivityEspressoTest {

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
    public IntentsTestRule<PersonDetailActivity> mActivityRule =
            new IntentsTestRule<>(PersonDetailActivity.class, false, false);

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
        b.putLong(ClazzListPresenter.ARG_CLAZZ_UID, testClazz.getClazzUid());
        b.putLong(PersonDetailView.ARG_PERSON_UID, personUid);
        launchActivityIntent.putExtras(b);
        mActivityRule.launchActivity(launchActivityIntent);

    }

    @Test
    public void givenActivityStarted_whenUserDetailsLoaded_shouldShowPersonFieldsAndCustomFields() {
        Assert.assertTrue(true);

        //Core fields are displayed as per Dao - value, icon

        //Custom fields are displayed as per Dao - value, icon

        //User name and image loaded ok
    }

    @Test
    public void givenActivityStarted_whenClickFatherMotherCallAndSms_shouldFireCallAndSmsIntentWithNumber(){

        //Click father number's phone icon  - should fire call intent with number as per core field.
        //Click mother number's phone icon - should fire call intent with number as per core field.

        //Click father's sms icon - should fire sms intent with number as per core field.
        //Click mother's sms icon - should fire sms intent with number as per core field.

    }

    @Test
    public void givenActivityStarted_whenEditButtonPressed_shouldOpenPersonEditActivitWithDetailsFilled(){

        //Click the Edit button - verify PersonEditActivity opened.

        //Verify Person Uid sent to PersonEditActivity.

    }

    @Test
    public void givenActivityStarted_whenEnrollInClassClicked_shouldOpenClassEnrollmentActivity(){

        //Click the "Enroll in class" top buttons - verify Class enrollment activity opened.

        //Verify Person Uid sent to the new class enrollment activity.
    }

    @Test
    public void givenActivityStarted_whenAttendanceReportClicked_shouldOpenAttendanceReportActivty(){

        //Click the "Attendance Report" top button - verify goes to new activity.

        //Verify person uid sent to the new activity.

    }

    @Test
    public void givenActivityStarted_whenRecordDropoutClicked_shouldOpenConfirmationAndRecordsDropout(){
        //Click the "Record Dropout" button - verify opens confirmation

        //Click confirm - verify person is drop outed - via Dao.
        //TODO: Make a test to be sure in the clazz student list we exclude that are not drop outed

    }

    @Test
    public void givenActivityStarted_whenCallAndTextParentClicked_shouldFireDialogAndCallAndTextIntent(){

        //Click Call parent - should open dialog to choose which parent
        //Click a parent  - verify call intent started.

        //Click Text parent - should open dialog to choose which parent
        //Click a parent - verify sms intent started.
    }

}
