package com.ustadmobile.test.port.android.testutil;

import com.ustadmobile.core.db.dao.ClazzActivityChangeDao;
import com.ustadmobile.core.db.dao.SelQuestionDao;
import com.ustadmobile.core.db.dao.SelQuestionSetDao;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.db.dao.ClazzLogDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.FeedEntryDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonDetailPresenterFieldDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzActivityChange;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.FeedEntry;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonField;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField;
import com.ustadmobile.lib.db.entities.SelQuestion;
import com.ustadmobile.lib.db.entities.SelQuestionSet;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.ustadmobile.lib.db.entities.PersonField.FIELD_TYPE_HEADER;
import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.CUSTOM_FIELD_MIN_UID;

/**
 * Database related method util go here. eg: setting the database with fixtures, etc.
 */
public class UmDbTestUtil {

    public static final String TEST_FEED_DESCRIPTION = "This is a test feed. Created from the tests.";

    public static List<HeadersAndFields> allFields = new ArrayList<>();

    public static List<String> customFieldValues = new ArrayList<>();

    public static List<PersonField> customFieldsCreated = null;

    public static boolean createdSELQuestions = false;

    static {

        //Core fields:

        //PROFILE/BASIC DETAILS
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
               PersonField.FIELD_TYPE_TEXT,
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
                PersonField.FIELD_TYPE_TEXT,
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
                PersonField.FIELD_TYPE_TEXT,
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
                PersonField.FIELD_TYPE_DATE,
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
                PersonField.FIELD_TYPE_TEXT,
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
                PersonField.FIELD_TYPE_TEXT,
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
                PersonField.FIELD_TYPE_PHONE_NUMBER,
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
                PersonField.FIELD_TYPE_TEXT,
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
                PersonField.FIELD_TYPE_PHONE_NUMBER,
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
                PersonField.FIELD_TYPE_TEXT,
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
                PersonField.FIELD_TYPE_PHONE_NUMBER,
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
                PersonField.FIELD_TYPE_TEXT,
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
//        allFields.add(new HeadersAndFields(
//                "ic_people_black_24dp",
//                "Classes",
//                MessageID.clazz,
//                PersonDetailPresenterField.PERSON_FIELD_UID_CLASSES,
//                18,
//                PersonDetailViewField.FIELD_TYPE_TEXT,
//                0,
//                false,
//                true,
//                true
//        ));


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
                PersonField.FIELD_TYPE_TEXT,
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
                PersonField.FIELD_TYPE_TEXT,
                0,
                false,
                true,
                true
        );
        allFields.add(cf2);

        //Setting test custom values.
        customFieldValues.add("42%");
        customFieldValues.add("Springfield Elementary School");

    }

    public static Clazz createDummyData(Object context){
        String TEST_CLASS_NAME = "Class A";
        float TEST_CLASS_PERCENTAGE = 0.42F;
        long TEST_USER_UID = 1L;
        String TEST_CLASS_MEMBER1_NAME = "Test User1";
        float TEST_CLASS_MEMBER1_PERCENTAGE = 0.15F;
        float TEST_CLASS_MEMBER2_PERCENTAGE = 0.35F;
        float TEST_CLASS_MEMBER3_PERCENTAGE = 0.55F;
        float TEST_CLASS_MEMBER4_PERCENTAGE = 0.85F;
        String TEST_CLASS_MEMBER2_NAME = "Test User2";
        String TEST_CLASS_MEMBER3_NAME = "Test User3";
        String TEST_CLASS_MEMBER4_NAME = "Test User4";
        Hashtable peopleMap;

        Clazz testClazz;

        peopleMap = new Hashtable();
        peopleMap.put(TEST_CLASS_MEMBER1_NAME, TEST_CLASS_MEMBER1_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER2_NAME, TEST_CLASS_MEMBER2_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER3_NAME, TEST_CLASS_MEMBER3_PERCENTAGE);
        peopleMap.put(TEST_CLASS_MEMBER4_NAME, TEST_CLASS_MEMBER4_PERCENTAGE);


        //Populate the database with test class:
        testClazz = createClazzWithClazzMembers(TEST_CLASS_NAME, TEST_CLASS_PERCENTAGE,
        peopleMap, TEST_USER_UID, context);
        return testClazz;
    }

    /**
     * Just a POJO for this test class to loop through and create the fields.
     */
    public static class HeadersAndFields {

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


    public static void addData(boolean clearDb, Object context){

        if (clearDb){
            UmAppDatabase.getInstance(context).clearAllTables();
        }

    }

    /**
     * Creates the custom fields from the fields set (which includes both core and custom).
     *
     * @param fields    List of CustomFields that contains both Headers of Core fields as well as
     *                  fields and headers of custom fields.
     * @param context   Android context.
     */
    public static List<PersonField> createCustomFields(List<HeadersAndFields> fields, Object context){
        PersonCustomFieldDao personCustomFieldDao =
                UmAppDatabase.getInstance(context).getPersonCustomFieldDao();
        PersonDetailPresenterFieldDao personDetailPresenterFieldDao =
                UmAppDatabase.getInstance(context).getPersonDetailPresenterFieldDao();
        PersonCustomFieldValueDao personCustomFieldValueDao =
                UmAppDatabase.getInstance(context).getPersonCustomFieldValueDao();

        customFieldsCreated = new ArrayList<>();

        for (HeadersAndFields field: fields){
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

        return customFieldsCreated;

    }

    /**
     * Sets the values to the custom fields given that have valid field uids
     *
     * @param person
     * @param customFieldsCreated
     * @param values
     * @param context
     */
    public static void setCustomFieldValue(Person person, List<PersonField> customFieldsCreated,
                                           List<String> values, Object context){
        Iterator<PersonField> cfi = customFieldsCreated.iterator();
        Iterator<String> cvi = values.iterator();
        PersonCustomFieldValueDao personCustomFieldValueDao =
                UmAppDatabase.getInstance(context).getPersonCustomFieldValueDao();
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
    }

    public static Person createPersonWithFieldsAndCustomFields(Object context){
        return createPersonWithFieldsAndCustomFields(null, context);
    }

    /**
     * Creates a test person with custom fields set by default.
     *
     * @param context   Android context
     * @return  Returns the person created and fields associated for entity.
     */
    public static Person createPersonWithFieldsAndCustomFields(Clazz toClazz, Object context){

        PersonDao personDao = UmAppDatabase.getInstance(context).getPersonDao();
        ClazzDao clazzDao = UmAppDatabase.getInstance(context).getClazzDao();
        ClazzMemberDao clazzMemberDao = UmAppDatabase.getInstance(context).getClazzMemberDao();

        //Create the person and set its core fields.
        Person newPerson = new Person();
        newPerson.setFirstNames("Raina Ali");
        newPerson.setLastName("Rawazi");
        newPerson.setEmailAddr("raina.rawazi@alifamily.com");
        newPerson.setGender(Person.GENDER_FEMALE);
        newPerson.setUsername("rainarawazi");
        newPerson.setActive(true);
        newPerson.setDateOfBirth(UMCalendarUtil.getLongDateFromPrettyString(
                "12-Jan-2001", Locale.US));
        newPerson.setFatherName("Addulla Rawazi");
        newPerson.setMotherName("Aysha Rawazi");
        newPerson.setFatherNumber("+96212345678");
        newPerson.setMotherNum("+96287654321");
        newPerson.setAddress("123 Fourth Street, FiftySix Avenue, SevenCity, Eightland");
        newPerson.setPersonUid(personDao.insert(newPerson));


        //Create Custom fields and their headings
        if(customFieldsCreated == null){
            customFieldsCreated = createCustomFields(allFields, context);
        }

        //Create values based on the created custom fields
        setCustomFieldValue(newPerson, customFieldsCreated, customFieldValues, context);

        if(toClazz == null){
            return newPerson;
        }

        //Assign person to given class.
        //Create ClazzMember
        ClazzMember clazzMember = new ClazzMember();
        clazzMember.setClazzMemberClazzUid(toClazz.getClazzUid());
        clazzMember.setRole(ClazzMember.ROLE_STUDENT);

        clazzMember.setClazzMemberPersonUid(newPerson.getPersonUid());
        clazzMember.setAttendancePercentage(0.42f);
        clazzMemberDao.insert(clazzMember);

        return newPerson;

    }

    public static void createDefaultQuestionSet(Object context){
        SelQuestionSetDao selQuestionSetDao =
                UmAppDatabase.getInstance(context).getSocialNominationQuestionSetDao();
        SelQuestionSet selQuestionSet = new SelQuestionSet();
        selQuestionSet.setTitle("Default set");
        selQuestionSetDao.insert(selQuestionSet);
        
    }
    
    public static void createDefaultQuestionSetAndSomeQuestions(Object context){
        SelQuestionSetDao questionSetDao =
                UmAppDatabase.getInstance(context).getSocialNominationQuestionSetDao();
        SelQuestionSet questionSet = new SelQuestionSet();
        questionSet.setTitle("Default set");
        questionSet.setSocialNominationQuestionSetUid(questionSetDao.insert(questionSet));

        SelQuestionDao questionDao =
                UmAppDatabase.getInstance(context).getSocialNominationQuestionDao();
        SelQuestion question1 = new SelQuestion();
        question1.setSocialNominationQuestionSocialNominationQuestionSetUid(
                questionSet.getSocialNominationQuestionSetUid());
        question1.setQuestionIndex(1);
        question1.setQuestionText("Who sits alone in the class?");
        question1.setMultiNominations(true);
        question1.setAssignToAllClasses(true);
        questionDao.insert(question1);

        SelQuestion question2 = new SelQuestion();
        question2.setSocialNominationQuestionSocialNominationQuestionSetUid(
                questionSet.getSocialNominationQuestionSetUid());
        question2.setQuestionIndex(2);
        question2.setQuestionText("Who are your friends?");
        question2.setMultiNominations(true);
        question2.setAssignToAllClasses(true);
        questionDao.insert(question2);

        SelQuestion question3 = new SelQuestion();
        question3.setSocialNominationQuestionSocialNominationQuestionSetUid(
                questionSet.getSocialNominationQuestionSetUid());
        question3.setQuestionIndex(3);
        question3.setQuestionText("Who annoys you?");
        question3.setMultiNominations(true);
        question3.setAssignToAllClasses(true);
        questionDao.insert(question3);

    }

    /**
     * Adds activity change data to db
     * @param context
     */
    public static void addActivityChangeDetails(Object context){
        ClazzActivityChangeDao activityChangeDao =
                UmAppDatabase.getInstance(context).getClazzActivityChangeDao();
        ClazzActivityChange ac1 = new ClazzActivityChange();
        ac1.setClazzActivityUnitOfMeasure(ClazzActivityChange.UOM_FREQUENCY);
        ac1.setClazzActivityChangeTitle("Activity Change 1");
        ac1.setClazzActivityDesc("Testing Activity Change 1");
        activityChangeDao.insert(ac1);

        ClazzActivityChange ac2 = new ClazzActivityChange();
        ac2.setClazzActivityUnitOfMeasure(ClazzActivityChange.UOM_BINARY);
        ac2.setClazzActivityChangeTitle("Activity Change 2");
        ac2.setClazzActivityDesc("Testing Activity Change 2");
        activityChangeDao.insert(ac2);

        ClazzActivityChange ac3 = new ClazzActivityChange();
        ac3.setClazzActivityUnitOfMeasure(ClazzActivityChange.UOM_DURATION);
        ac3.setClazzActivityChangeTitle("Activity Change 3");
        ac3.setClazzActivityDesc("Testing Activity Change 3");
        activityChangeDao.insert(ac3);


    }
    

    /**
     * Creates a Class/Clazz with given name, assigns members with person Uid, sets percentage.
     * @param className The name of the Class / Clazz
     * @param classPercentage   The Class/Clazz attendance percentage
     * @param peopleMap Map of people to create ClazzMembers with
     * @param personUidAssigned The PersonUid override to have access.
     * @param context   The context.
     * @return  The Clazz Class object.
     */
    public static Clazz createClazzWithClazzMembers(String className, float classPercentage,
                                                    Hashtable peopleMap, long personUidAssigned,
                                                    Object context){
        ClazzDao clazzDao = UmAppDatabase.getInstance(context).getClazzDao();
        ClazzMemberDao clazzMemberDao = UmAppDatabase.getInstance(context).getClazzMemberDao();
        PersonDao personDao = UmAppDatabase.getInstance(context).getPersonDao();

        //Create the Clazz
        Clazz testClazz = new Clazz();
        testClazz.setClazzName(className);
        testClazz.setAttendanceAverage(classPercentage);
        testClazz.setClazzUid(clazzDao.insert(testClazz));

        if(personUidAssigned > 0){
            //Create a ClazzMember so that we can view the Classes. this is ideally logged in user
            // or teacher assigned to Class. For testing and start we hardcode it.
            ClazzMember loggedInClazzMember = new ClazzMember();
            loggedInClazzMember.setClazzMemberPersonUid(personUidAssigned);
            loggedInClazzMember.setClazzMemberClazzUid(testClazz.getClazzUid());
            loggedInClazzMember.setRole(ClazzMember.ROLE_TEACHER);
            loggedInClazzMember.setClazzMemberClazzUid(clazzMemberDao.insert(loggedInClazzMember));
        }

        Set<String> peopleMapKeys = peopleMap.keySet();
        for(String name: peopleMapKeys){
            //Create people
            //float personAttendancePercentage = Float.parseFloat(peopleMap.get(name).toString());
            float personAttendancePercentage = (float) peopleMap.get(name);
            Person testPerson = new Person();
            testPerson.setFirstNames(name.split(" ")[0]);
            testPerson.setLastName(name.split(" ")[1]);
            testPerson.setActive(true);
            testPerson.setPersonUid(personDao.insert(testPerson));

            //Create Custom fields and their headings
            if(customFieldsCreated == null){
                customFieldsCreated = createCustomFields(allFields, context);
            }

            //Create values based on the created custom fields
            setCustomFieldValue(testPerson, customFieldsCreated, customFieldValues, context);

            //Create ClazzMember
            ClazzMember clazzMember = new ClazzMember();
            clazzMember.setClazzMemberClazzUid(testClazz.getClazzUid());
            clazzMember.setRole(ClazzMember.ROLE_STUDENT);

            clazzMember.setClazzMemberPersonUid(testPerson.getPersonUid());
            clazzMember.setAttendancePercentage(personAttendancePercentage);
            clazzMemberDao.insert(clazzMember);

        }

        if(!createdSELQuestions){
            createDefaultQuestionSetAndSomeQuestions(context);
            createdSELQuestions = true;
        }

        return testClazz;

    }

    public static void createClazzLogs(Hashtable clazzLogsToCreate, Clazz thisClazz, Object context){
        //Create a ClassLog for today

        ClazzLogDao clazzLogDao = UmAppDatabase.getInstance(context).getClazzLogDao();
        ClazzLogAttendanceRecordDao clazzLogAttendanceRecordDao =
                UmAppDatabase.getInstance(context).getClazzLogAttendanceRecordDao();


        Set<Long> allDates = clazzLogsToCreate.keySet();
        for(Long thisDate : allDates){
            Hashtable peopleAttendanceMap = (Hashtable) clazzLogsToCreate.get(thisDate);

            ClazzLog newClazzLog = new ClazzLog();
            newClazzLog.setLogDate(thisDate);
            newClazzLog.setDone(false);
            newClazzLog.setClazzLogClazzUid(thisClazz.getClazzUid());
            newClazzLog.setClazzLogUid(clazzLogDao.insert(newClazzLog));

            clazzLogAttendanceRecordDao.insertAllAttendanceRecords(thisClazz.getClazzUid(),
                    newClazzLog.getClazzLogUid(), null);

        }
    }

    public static void createFeedEntries(Hashtable feedsToCreate, long feedClazzUid,
                                               long feedEntryPersonUid, Object context) {
        FeedEntryDao feedEntryDao = UmAppDatabase.getInstance(context).getFeedEntryDao();

        Set<String> feedMapKeys = feedsToCreate.keySet();
        for(String feedTitle: feedMapKeys){
            long feedLogDate = (long)feedsToCreate.get(feedTitle);
            String feedEntryLink = ClassLogDetailView.VIEW_NAME + "?" +
                    ClazzListView.ARG_CLAZZ_UID + "=" + feedClazzUid + "&" +
                    ClazzListView.ARG_LOGDATE + "=" + feedLogDate;
            long feedEntryHash = 456;

            FeedEntry thisFeedEntry =  new FeedEntry();
            thisFeedEntry.setDescription(TEST_FEED_DESCRIPTION);
            thisFeedEntry.setTitle(feedTitle);
            thisFeedEntry.setDeadline(feedLogDate);
            thisFeedEntry.setFeedEntryPersonUid(feedEntryPersonUid);
            thisFeedEntry.setLink(feedEntryLink);
            thisFeedEntry.setFeedEntryHash(feedEntryHash);
            thisFeedEntry.setFeedEntryUid(feedEntryDao.insert(thisFeedEntry));

        }
    }

}
