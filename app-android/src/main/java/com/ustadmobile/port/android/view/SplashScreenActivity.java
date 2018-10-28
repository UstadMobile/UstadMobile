/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */

package com.ustadmobile.port.android.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzListPresenter;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.FeedEntryDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonDetailPresenterFieldDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionSetDao;
import com.ustadmobile.core.db.dao.UMCalendarDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.core.view.PersonDetailViewField;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.FeedEntry;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField;
import com.ustadmobile.lib.db.entities.PersonField;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSet;
import com.ustadmobile.lib.db.entities.UMCalendar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.ustadmobile.core.view.PersonDetailViewField.FIELD_TYPE_HEADER;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.CUSTOM_FIELD_MIN_UID;


public class SplashScreenActivity extends AppCompatActivity
        implements DialogInterface.OnClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback{

    public static final int EXTERNAL_STORAGE_REQUESTED = 1;

    public static final String[] REQUIRED_PERMISSIONS = new String[]{
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    };

    boolean rationalesShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        addDummyData();
    }

    /**
     * Calls startUi to be run. This is usually called after we have checked permissions.
     */
    public void startTheUI(){
        new Handler().postDelayed(
                () -> UstadMobileSystemImpl.getInstance()
                        .startUI(SplashScreenActivity.this), 0);
    }

    /**
     * Checks for permissions and alerts the user to give permissions.
     */
    public void checkPermissions() {
        boolean hasRequiredPermissions = true;
        for(int i = 0; i < REQUIRED_PERMISSIONS.length; i++) {
            hasRequiredPermissions &=
                    ContextCompat.checkSelfPermission(this,
                            REQUIRED_PERMISSIONS[i]) == PackageManager.PERMISSION_GRANTED;
        }

        if(!hasRequiredPermissions){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) && !rationalesShown) {
                //show an alert
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("File permissions required")
                        .setMessage("This app requires file permissions " +
                                "on the SD card to download and save content");
                builder.setPositiveButton("OK", this);
                AlertDialog dialog = builder.create();
                dialog.show();
                rationalesShown = true;
                return;
            }else {
                rationalesShown = false;
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                        EXTERNAL_STORAGE_REQUESTED);
                return;
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        boolean allGranted = permissions.length == 2;
        for(int i = 0; i < grantResults.length; i++) {
            allGranted &= grantResults[i] == PackageManager.PERMISSION_GRANTED;
        }

        if(allGranted) {
            new Handler().postDelayed(
                    () -> UstadMobileSystemImpl.getInstance()
                            .startUI(SplashScreenActivity.this), 0);
        }else {
            /* avoid possibly getting into an infinite loop if we had no user interaction
                and permission was denied
             */
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try { Thread.sleep(500); }
                    catch(InterruptedException e) {}
                    return null;
                }

                @Override
                protected void onPostExecute(Void o) {
                    SplashScreenActivity.this.checkPermissions();
                }
            }.execute();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        //Disabling for now in the new version of the application
        //checkPermissions();
        startTheUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_leavecontainer) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * Adds dummy data in the start of the application here. It also sets a key so that we don't
     * add the dummy data every time. This will get replaced with real data that will sync with
     * the server.
     */
    public void addDummyData(){
        String createStatus = UstadMobileSystemImpl.getInstance().getAppPref("dummydata",
                getApplicationContext());
        if(createStatus != null)
            return;

        ClazzDao clazzDao =
                UmAppDatabase.getInstance(getApplicationContext()).getClazzDao();
        ClazzMemberDao clazzMemberDao = UmAppDatabase.getInstance(getApplicationContext())
                .getClazzMemberDao();
        PersonDao personDao =
                UmAppDatabase.getInstance(getApplicationContext()).getPersonDao();



        //Running all insertions in a separate thread.
        new Thread(() -> {

            //Sprint 2 stuff:

            /**
             * Just a POJO for this test class to loop through and create the fields.
             */
             class HeadersAndFields {

                public HeadersAndFields(String fieldIcon, String fieldName, int fieldLabel, int fieldUid,
                                        int fieldIndex, int fieldType, int headerMessageId, boolean readOnly,
                                        boolean viewMode, boolean editMode){

                    this.fieldIcon = fieldIcon;
                    this.fieldName = fieldName;
                    this.fieldLabel = fieldLabel;
                    this.fieldUid = fieldUid;
                    this.fieldType = fieldType;
                    this.fieldIndex = fieldIndex;
                    this.headerMessageId = headerMessageId;
                    this.readOnly = readOnly;
                    this.viewMode = viewMode;
                    this.editMode = editMode;
                }


                //field uid
                public int fieldUid;
                //icon
                public String fieldIcon;
                //random name
                public String fieldName;
                //label
                public int fieldLabel;
                //type (field/header)
                public int fieldType;
                //index (order)
                public int fieldIndex;
                //header label (if applicable)
                public int headerMessageId;

                public boolean readOnly;

                public boolean viewMode;

                public boolean editMode;


            }

             List<HeadersAndFields> allFields = new ArrayList<>();
             List<String> customFieldValues = new ArrayList<>();
             List<PersonField> customFieldsCreated = null;

            allFields.add(new HeadersAndFields(
                    "",
                    "",
                    0,
                    0,
                    1,
                    FIELD_TYPE_HEADER,
                    MessageID.profile,
                    false,
                    true,
                    true
            ));
            allFields.add(new HeadersAndFields(
                    "",
                    "Full Name",
                    MessageID.field_fullname,
                    PersonDetailPresenterField.PERSON_FIELD_UID_FULL_NAME,
                    2,
                    PersonDetailViewField.FIELD_TYPE_TEXT,
                    0,
                    false,
                    true,
                    false
            ));

            ///FIRST NAME LAST NAME
            allFields.add(new HeadersAndFields(
                    "ic_person_black_24dp",
                    "First Names",
                    MessageID.first_names,
                    PersonDetailPresenterField.PERSON_FIELD_UID_FIRST_NAMES,
                    3,
                    PersonDetailViewField.FIELD_TYPE_TEXT,
                    0,
                    false,
                    false,
                    true
            ));
            allFields.add(new HeadersAndFields(
                    "",
                    "Last Name",
                    MessageID.last_name,
                    PersonDetailPresenterField.PERSON_FIELD_UID_LAST_NAME,
                    4,
                    PersonDetailViewField.FIELD_TYPE_TEXT,
                    0,
                    false,
                    false,
                    true
            ));

            //BIRTHDAY
            allFields.add(new HeadersAndFields(
                    "ic_perm_contact_calendar_black_24dp",
                    "Date of Birth",
                    MessageID.birthday,
                    PersonDetailPresenterField.PERSON_FIELD_UID_BIRTHDAY,
                    5,
                    PersonDetailViewField.FIELD_TYPE_DATE,
                    0,
                    false,
                    true,
                    true
            ));
            //ADDRESS
            allFields.add(new HeadersAndFields(
                    "",
                    "Home Address",
                    MessageID.home_address,
                    PersonDetailPresenterField.PERSON_FIELD_UID_ADDRESS,
                    6,
                    PersonDetailViewField.FIELD_TYPE_TEXT,
                    0,
                    false,
                    true,
                    true
            ));

            //ATTENDANCE
            allFields.add(new HeadersAndFields(
                    "",
                    "",
                    0,
                    0,
                    7,
                    FIELD_TYPE_HEADER,
                    MessageID.attendance,
                    false,
                    true,
                    false
            ));
            allFields.add(new HeadersAndFields(
                    "ic_lens_black_24dp",
                    "Total Attendance for student and days",
                    MessageID.attendance,
                    PersonDetailPresenterField.PERSON_FIELD_UID_ATTENDANCE,
                    8,
                    PersonDetailViewField.FIELD_TYPE_TEXT,
                    0,
                    false,
                    true,
                    false
            ));

            //PARENTS
            allFields.add(new HeadersAndFields(
                    "ic_person_black_24dp",
                    "Father with number",
                    MessageID.father,
                    PersonDetailPresenterField.PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER,
                    11,
                    PersonDetailViewField.FIELD_TYPE_PHONE_NUMBER,
                    0,
                    false,
                    true,
                    false
            ));
            allFields.add(new HeadersAndFields(
                    "ic_person_black_24dp",
                    "Father name",
                    MessageID.fathers_name,
                    PersonDetailPresenterField.PERSON_FIELD_UID_FATHER_NAME,
                    12,
                    PersonDetailViewField.FIELD_TYPE_TEXT,
                    0,
                    false,
                    false,
                    true
            ));
            allFields.add(new HeadersAndFields(
                    "ic_person_black_24dp",
                    "Father  number",
                    MessageID.fathers_number,
                    PersonDetailPresenterField.PERSON_FIELD_UID_FATHER_NUMBER,
                    13,
                    PersonDetailViewField.FIELD_TYPE_PHONE_NUMBER,
                    0,
                    false,
                    false,
                    true
            ));
            allFields.add(new HeadersAndFields(
                    "ic_person_black_24dp",
                    "Mother name",
                    MessageID.mothers_name,
                    PersonDetailPresenterField.PERSON_FIELD_UID_MOTHER_NAME,
                    14,
                    PersonDetailViewField.FIELD_TYPE_TEXT,
                    0,
                    false,
                    false,
                    true
            ));
            allFields.add(new HeadersAndFields(
                    "ic_person_black_24dp",
                    "Mother number",
                    MessageID.mothers_number,
                    PersonDetailPresenterField.PERSON_FIELD_UID_MOTHER_NUMBER,
                    15,
                    PersonDetailViewField.FIELD_TYPE_PHONE_NUMBER,
                    0,
                    false,
                    false,
                    true
            ));
            allFields.add(new HeadersAndFields(
                    "ic_person_black_24dp",
                    "Mother with number",
                    MessageID.mother,
                    PersonDetailPresenterField.PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER,
                    16,
                    PersonDetailViewField.FIELD_TYPE_TEXT,
                    0,
                    false,
                    true,
                    false
            ));

            //CLASSES
            allFields.add(new HeadersAndFields(
                    "",
                    "",
                    0,
                    0,
                    17,
                    FIELD_TYPE_HEADER,
                    MessageID.classes,
                    false,
                    true,
                    true
            ));

            //Custom fields:
            allFields.add(new HeadersAndFields(
                    "",
                    "",
                    0,
                    0,
                    19,
                    FIELD_TYPE_HEADER,
                    MessageID.background,
                    false,
                    true,
                    true
            ));

            HeadersAndFields cf1 = new HeadersAndFields(
                    "ic_done_all_black_24dp",
                    "ASER test result",
                    MessageID.aser_test_result,
                    0,
                    20,
                    PersonDetailViewField.FIELD_TYPE_TEXT,
                    0,
                    false,
                    true,
                    true
            );

            allFields.add(cf1);
            HeadersAndFields cf2 = new HeadersAndFields(
                    "ic_account_balance_black_24dp",
                    "Schooling",
                    MessageID.current_formal_school,
                    0,
                    21,
                    PersonDetailViewField.FIELD_TYPE_TEXT,
                    0,
                    false,
                    true,
                    true
            );
            allFields.add(cf2);

            //Setting test custom values. //Sprint 2 stuff:
            customFieldValues.add("42%");
            customFieldValues.add("Rjem Hussein Public school");

            //Create Custom Fields:
            PersonCustomFieldDao personCustomFieldDao =
                    UmAppDatabase.getInstance(getApplicationContext()).getPersonCustomFieldDao();
            PersonDetailPresenterFieldDao personDetailPresenterFieldDao =
                    UmAppDatabase.getInstance(getApplicationContext()).getPersonDetailPresenterFieldDao();
            PersonCustomFieldValueDao personCustomFieldValueDao =
                    UmAppDatabase.getInstance(getApplicationContext()).getPersonCustomFieldValueDao();

            customFieldsCreated = new ArrayList<>();

            for (HeadersAndFields field: allFields){
                boolean isHeader = false;
                if(field.fieldType == FIELD_TYPE_HEADER){
                    isHeader = true;
                }

                //Create the custom fields - basically label & icon .
                PersonField pcf1 = new PersonField();

                //Create the field only if it is a field (ie not a header)
                if (!isHeader) {
                    pcf1.setFieldIcon(field.fieldIcon); //Icon
                    pcf1.setFieldName(field.fieldName); //Internal name
                    pcf1.setLabelMessageId(field.fieldLabel);    //Label
                    //If field not set ie its a Custom Field
                    if(field.fieldUid ==0){
                        int lastPersonCustomFieldUidUsed = personCustomFieldDao.findLatestUid();
                        int newCustomPersonCustomFieldUid = lastPersonCustomFieldUidUsed + 1;
                        if(lastPersonCustomFieldUidUsed < CUSTOM_FIELD_MIN_UID){
                            //first Custom field
                            newCustomPersonCustomFieldUid =
                                    CUSTOM_FIELD_MIN_UID + 1;
                        }
                        pcf1.setPersonCustomFieldUid(newCustomPersonCustomFieldUid);
                        field.fieldUid = newCustomPersonCustomFieldUid;
                        customFieldsCreated.add(pcf1);
                    }else {
                        pcf1.setPersonCustomFieldUid(field.fieldUid);   //Field's uid
                    }

                    personCustomFieldDao.insert(pcf1);  //Persist
                }

                //Create the Mapping between the fields and extra information like :
                //  type(header / field)
                //  index (for ordering)
                //  Header String Id (if header)
                //
                PersonDetailPresenterField pdpf1 = new PersonDetailPresenterField();
                pdpf1.setFieldType(field.fieldType);
                pdpf1.setFieldIndex(field.fieldIndex);

                pdpf1.setFieldIcon(field.fieldIcon);
                pdpf1.setLabelMessageId(field.fieldLabel);

                //Set Visibility
                pdpf1.setReadyOnly(field.readOnly);
                pdpf1.setViewModeVisible(field.viewMode);
                pdpf1.setEditModeVisible(field.editMode);

                //If not a header set the field. If is header, set the header label.
                if(!isHeader) {
                    pdpf1.setFieldUid(pcf1.getPersonCustomFieldUid());
                }else {
                    pdpf1.setHeaderMessageId(field.headerMessageId);
                }

                //persist:
                pdpf1.setPersonDetailPresenterFieldUid(personDetailPresenterFieldDao.insert(pdpf1));

            }


            //Create Class
            Clazz clazz1 = new Clazz();
            clazz1.setClazzName("Class A");
            clazz1.setAttendanceAverage(0L);
            clazz1.setClazzActive(true);
            long thisClazzUid = clazzDao.insert(clazz1);
            clazz1.setClazzUid(thisClazzUid);


            //Names
            String[] names = {"Shukriyya al-Azzam", "Ummu Kulthoom al-Munir","Azeema el-Saleem",
                    "Fuaada el-Qadir","Amatullah al-Baluch","Sham'a al-Wali","Haamida al-Sinai",
                    "Hasnaa el-Khalili","Nawwaara al-Salim","Qamraaa el-Shakoor",
                    "Riyaal al-Moustafa","Haazim al-Salah","Hamdaan el-Ishmael","Baheej el-Huda",
                    "Mahdi el-Mahmoud","Badraan el-Zaman","Saeed el-Rafiq","Husaam el-Wakim",
                    "Mansoor el-Saidi","Nazmi al-Hares"};

            //Persist names to DB <-> ClazzMembers <-> Clazz
            for (String full_name: names){
                //Create Person
                String first_name = full_name.split(" ")[0];
                String last_name = full_name.split(" ")[1];
                Person person = new Person();
                person.setActive(true);
                person.setFirstNames(first_name);
                person.setLastName(last_name);


                person.setEmailAddr(first_name + last_name + "@ustadmobile.com");
                person.setGender(Person.GENDER_FEMALE);
                person.setDateOfBirth(UMCalendarUtil.getLongDateFromPrettyString("12-Jan-2001"));
                person.setFatherName("Addulla " + last_name);
                person.setMotherName("Aysha " + last_name);
                person.setFatherNumber("+96212345678");
                person.setMotherNum("+96287654321");
                person.setAddress("123 Fourth Street, FiftySix Avenue, SevenCity, Eightland");


                long thisPersonUid = personDao.insert(person);
                person.setPersonUid(thisPersonUid);


                //Add Custom Fields as well:

                //Sprint 2 stuff:
                //Create values based on the created custom fields
                Iterator<PersonField> cfi = customFieldsCreated.iterator();
                Iterator<String> cvi = customFieldValues.iterator();

                while(cfi.hasNext() && cvi.hasNext()){
                    PersonField cf = cfi.next();
                    String cv = cvi.next();

                    PersonCustomFieldValue personCustomFieldValue = new PersonCustomFieldValue();
                    personCustomFieldValue.setPersonCustomFieldValuePersonUid(person.getPersonUid());
                    personCustomFieldValue.setPersonCustomFieldValuePersonCustomFieldUid(
                            cf.getPersonCustomFieldUid());
                    personCustomFieldValue.setFieldValue(cv);
                    personCustomFieldValue.setPersonCustomFieldValueUid(
                            personCustomFieldValueDao.insert(personCustomFieldValue));

                }


                //Create ClazzMember
                ClazzMember member = new ClazzMember();
                member.setRole(ClazzMember.ROLE_STUDENT);
                member.setClazzMemberClazzUid(clazz1.getClazzUid());
                member.setClazzMemberPersonUid(thisPersonUid);
                member.setAttendancePercentage(0L);
                member.setClazzMemberActive(true);
                clazzMemberDao.insertAsync(member, null);
            }

            //Sprint 2 stuff:

            //Create SEL questions :
            SocialNominationQuestionSetDao questionSetDao =
                    UmAppDatabase.getInstance(getApplicationContext()).getSocialNominationQuestionSetDao();
            SocialNominationQuestionSet questionSet = new SocialNominationQuestionSet();
            questionSet.setTitle("Default set");
            questionSet.setSocialNominationQuestionSetUid(questionSetDao.insert(questionSet));

            SocialNominationQuestionDao questionDao =
                    UmAppDatabase.getInstance(getApplicationContext()).getSocialNominationQuestionDao();
            SocialNominationQuestion question1 = new SocialNominationQuestion();
            question1.setSocialNominationQuestionSocialNominationQuestionSetUid(
                    questionSet.getSocialNominationQuestionSetUid());
            question1.setQuestionIndex(1);
            question1.setQuestionText("Who sits alone in the class?");
            question1.setMultiNominations(true);
            question1.setAssignToAllClasses(true);
            questionDao.insert(question1);

            SocialNominationQuestion question2 = new SocialNominationQuestion();
            question2.setSocialNominationQuestionSocialNominationQuestionSetUid(
                    questionSet.getSocialNominationQuestionSetUid());
            question2.setQuestionIndex(2);
            question2.setQuestionText("Who are your friends?");
            question2.setMultiNominations(true);
            question2.setAssignToAllClasses(true);
            questionDao.insert(question2);

            SocialNominationQuestion question3 = new SocialNominationQuestion();
            question3.setSocialNominationQuestionSocialNominationQuestionSetUid(
                    questionSet.getSocialNominationQuestionSetUid());
            question3.setQuestionIndex(3);
            question3.setQuestionText("Who annoys you?");
            question3.setMultiNominations(true);
            question3.setAssignToAllClasses(true);
            questionDao.insert(question3);


            //Create some feeds. The feeds upon interaction will in-turn create ClazzLogs.
            FeedEntryDao feedEntryDao =
                    UmAppDatabase.getInstance(getApplicationContext()).getFeedEntryDao();

            long feedClazzUid = clazz1.getClazzUid();
            long thisPersonUid = 1L;


            long thisDate = UMCalendarUtil.getDateInMilliPlusDays(0);
            FeedEntry thisFeed = new FeedEntry();
            thisFeed.setDeadline(thisDate);
            thisFeed.setFeedEntryDone(false);
            thisFeed.setDescription("This is your regular attendance alert.");
            thisFeed.setTitle("Record attendance for Class " + 1);
            thisFeed.setFeedEntryPersonUid(thisPersonUid);
            thisFeed.setLink(ClassLogDetailView.VIEW_NAME + "?" +
                    ClazzListPresenter.ARG_CLAZZ_UID + "=" + feedClazzUid + "&" +
                    ClazzListView.ARG_LOGDATE + "=" + thisDate);
            thisFeed.setFeedEntryHash(123);

            feedEntryDao.insertAsync(thisFeed, new UmCallback<Long>() {
                @Override
                public void onSuccess(Long result) {
                    long newDate = UMCalendarUtil.getDateInMilliPlusDays(-1);
                    FeedEntry newFeed = new FeedEntry();
                    newFeed.setDeadline(newDate);
                    newFeed.setFeedEntryDone(false);
                    newFeed.setDescription("This is your regular attendance alert.");
                    newFeed.setTitle("Record attendance for Class " + 1);
                    newFeed.setFeedEntryPersonUid(thisPersonUid);
                    newFeed.setLink(ClassLogDetailView.VIEW_NAME + "?" +
                            ClazzListPresenter.ARG_CLAZZ_UID + "=" + feedClazzUid + "&" +
                            ClazzListView.ARG_LOGDATE + "=" + newDate);
                    newFeed.setFeedEntryHash(456);
                    feedEntryDao.insertAsync(newFeed, null);
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });

            //Add Holiday Calendar
            UMCalendarDao calendarDao =
                    UmAppDatabase.getInstance(getApplicationContext()).getUMCalendarDao();
            UMCalendar newCalendar1 = new UMCalendar();
            newCalendar1.setUmCalendarName("IRC Holiday Calendar");
            newCalendar1.setUmCalendarUid(calendarDao.insert(newCalendar1));

            UMCalendar newCalendar2 = new UMCalendar();
            newCalendar2.setUmCalendarName("Lebanon Holiday Calendar");
            newCalendar2.setUmCalendarUid(calendarDao.insert(newCalendar2));


            //Set that we have created dummy data so that check for this and don't create it again.
            UstadMobileSystemImpl.getInstance().setAppPref("dummydata", "created",
                    getApplicationContext());
            UstadMobileSystemImpl.getInstance().startUI(SplashScreenActivity.this);
        }).start();

    }

}
