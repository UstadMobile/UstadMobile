package com.ustadmobile.test.port.android.testutil;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ClazzListPresenter;
import com.ustadmobile.core.controller.ClazzLogDetailPresenter;
import com.ustadmobile.core.controller.PersonDetailPresenter;
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
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.FeedEntry;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonCustomField;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Database related method util go here. eg: setting the database with fixtures, etc.
 */
public class UmDbTestUtil {

    public static final String TEST_FEED_DESCRIPTION = "This is a test feed. Created from the tests.";

    public static List<CustomField> allFields = new ArrayList<>();

    public static List<String> customFieldValues = new ArrayList<>();

    static {

        //Core fields:
        allFields.add(new CustomField(
                "",
                "First Names",
                MessageID.first_names,
                PersonDetailPresenterField.PERSON_FIELD_UID_FIRST_NAMES,
                1,
                PersonDetailPresenter.PersonDetailViewField.FIELD_TYPE_TEXT,
                0
        ));
        allFields.add(new CustomField(
                "",
                "Last Name",
                MessageID.last_name,
                PersonDetailPresenterField.PERSON_FIELD_UID_LAST_NAME,
                2,
                PersonDetailPresenter.PersonDetailViewField.FIELD_TYPE_TEXT,
                0
        ));
        allFields.add(new CustomField(
                "",
                "",
                0,
                0,
                3,
                PersonDetailPresenterField.FIELD_TYPE_HEADER,
                MessageID.attendance
        ));
        allFields.add(new CustomField(
                "ic_lens_black_24dp",
                "Total Attendance for student and days",
                MessageID.attendance,
                PersonDetailPresenterField.PERSON_FIELD_UID_ATTENDANCE,
                4,
                PersonDetailPresenter.PersonDetailViewField.FIELD_TYPE_TEXT,
                0
        ));
        allFields.add(new CustomField(
                "",
                "",
                0,
                0,
                5,
                PersonDetailPresenterField.FIELD_TYPE_HEADER,
                MessageID.classes
        ));
        allFields.add(new CustomField(
                "ic_people_black_24dp",
                "Classes",
                MessageID.attendance,
                PersonDetailPresenterField.PERSON_FIELD_UID_CLASSES,
                6,
                PersonDetailPresenter.PersonDetailViewField.FIELD_TYPE_TEXT,
                0
        ));
        allFields.add(new CustomField(
                "",
                "",
                0,
                0,
                7,
                PersonDetailPresenterField.FIELD_TYPE_HEADER,
                MessageID.profile
        ));
        allFields.add(new CustomField(
                "ic_person_black_24dp",
                "Father with number",
                MessageID.father,
                PersonDetailPresenterField.PERSON_FIELD_UID_FATHER_NAME_AND_PHONE_NUMBER,
                8,
                PersonDetailPresenter.PersonDetailViewField.FIELD_TYPE_TEXT,
                0
        ));
        allFields.add(new CustomField(
                "ic_person_black_24dp",
                "Mother with number",
                MessageID.mother,
                PersonDetailPresenterField.PERSON_FIELD_UID_MOTHER_NAME_AND_PHONE_NUMBER,
                9,
                PersonDetailPresenter.PersonDetailViewField.FIELD_TYPE_TEXT,
                0
        ));
        allFields.add(new CustomField(
                "ic_perm_contact_calendar_black_24dp",
                "Date of Birth",
                MessageID.birthday,
                PersonDetailPresenterField.PERSON_FIELD_UID_BIRTHDAY,
                10,
                PersonDetailPresenter.PersonDetailViewField.FIELD_TYPE_TEXT,
                0
        ));

        allFields.add(new CustomField(
                "",
                "",
                0,
                0,
                11,
                PersonDetailPresenterField.FIELD_TYPE_HEADER,
                MessageID.background
        ));

        //Custom fields:
        CustomField cf1 = new CustomField(
                "ic_done_all_black_24dp",
                "ASER test result",
                MessageID.aser_test_result,
                0,
                12,
                PersonDetailPresenter.PersonDetailViewField.FIELD_TYPE_TEXT,
                0
        );

        allFields.add(cf1);
        CustomField cf2 = new CustomField(
                "ic_account_balance_black_24dp",
                "Schooling",
                MessageID.current_formal_school,
                0,
                13,
                PersonDetailPresenter.PersonDetailViewField.FIELD_TYPE_TEXT,
                0
        );
        allFields.add(cf2);


        customFieldValues.add("42%");
        customFieldValues.add("Springfield Elementary School");
    }

    public static class CustomField {

        public CustomField(String fieldIcon, String fieldName, int fieldLabel, int fieldUid,
                           int fieldIndex, int fieldType, int headerMessageId){

            this.fieldIcon = fieldIcon;
            this.fieldName = fieldName;
            this.fieldLabel = fieldLabel;
            this.fieldUid = fieldUid;
            this.fieldType = fieldType;
            this.fieldIndex = fieldIndex;
            this.headerMessageId = headerMessageId;
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
    public static List<PersonCustomField> createCustomFields(List<CustomField> fields, Object context){
        PersonCustomFieldDao personCustomFieldDao =
                UmAppDatabase.getInstance(context).getPersonCustomFieldDao();
        PersonDetailPresenterFieldDao personDetailPresenterFieldDao =
                UmAppDatabase.getInstance(context).getPersonDetailPresenterFieldDao();
        PersonCustomFieldValueDao personCustomFieldValueDao =
                UmAppDatabase.getInstance(context).getPersonCustomFieldValueDao();
        List<PersonCustomField> customFieldsCreated = new ArrayList<>();

        for (CustomField field: fields){
            boolean isHeader = false;
            if(field.fieldType == PersonDetailPresenterField.FIELD_TYPE_HEADER){
                isHeader = true;
            }

            //Create the custom fields - basically label & icon .
            PersonCustomField pcf1 = new PersonCustomField();

            //Create the field only if it is a field (ie not a header)
            if (!isHeader) {
                pcf1.setFieldIcon(field.fieldIcon); //Icon
                pcf1.setFieldName(field.fieldName); //Internal name
                pcf1.setLabelMessageId(field.fieldLabel);    //Label
                //If field not set ie its a Custom Field
                if(field.fieldUid ==0){
                    int lastPersonCustomFieldUidUsed = personCustomFieldDao.findLatestUid();
                    int newCustomPersonCustomFieldUid = lastPersonCustomFieldUidUsed + 1;
                    if(lastPersonCustomFieldUidUsed < PersonDetailPresenterField.CUSTOM_FEILD_MIN_UID){
                        //first Custom field
                        newCustomPersonCustomFieldUid = PersonDetailPresenterField.CUSTOM_FEILD_MIN_UID + 1;
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
    public static void setCustomFieldValue(Person person, List<PersonCustomField> customFieldsCreated,
                                           List<String> values, Object context){
        Iterator<PersonCustomField> cfi = customFieldsCreated.iterator();
        Iterator<String> cvi = values.iterator();
        PersonCustomFieldValueDao personCustomFieldValueDao =
                UmAppDatabase.getInstance(context).getPersonCustomFieldValueDao();
        while(cfi.hasNext() && cvi.hasNext()){
            PersonCustomField cf = cfi.next();
            String cv = cvi.next();

            PersonCustomFieldValue personCustomFieldValue = new PersonCustomFieldValue();
            personCustomFieldValue.setPersonCustomFieldValuePersonUid(person.getPersonUid());
            personCustomFieldValue.setPersonCustomFieldValuePersonCustomFieldUid(cf.getPersonCustomFieldUid());
            personCustomFieldValue.setFieldValue(cv);
            personCustomFieldValue.setPersonCustomFieldValueUid(
                    personCustomFieldValueDao.insert(personCustomFieldValue));

        }
    }

    /**
     * Creates a test person with custom fields set by default.
     *
     * @param context   Android context
     * @return  Returns the person created and fields associated for entity.
     */
    public static Person createPersonWithFieldsAndCustomFields(Object context){

        //Create the person and set its non custom fields
        PersonDao personDao = UmAppDatabase.getInstance(context).getPersonDao();
        Person newPerson = new Person();
        newPerson.setFirstNames("Raina Ali");
        newPerson.setLastName("Rawazi");
        newPerson.setEmailAddr("raina.rawazi@alifamily.com");
        newPerson.setGender(Person.GENDER_FEMALE);
        newPerson.setUsername("rainarawazi");
        newPerson.setActive(true);
        newPerson.setDateOfBirth(UMCalendarUtil.getLongDateFromPrettyString("12-Jan-2001"));
        newPerson.setFatherName("Addulla Rawazi");
        newPerson.setMotherName("Aysha Rawazi");
        newPerson.setFatherNumber("+96212345678");
        newPerson.setMotherNum("+96287654321");
        newPerson.setPersonUid(personDao.insert(newPerson));


        //Create Custom fields and their headings
        List<PersonCustomField> customFieldsCreated = createCustomFields(allFields, context);

        //Create values based on the created custom fields
        setCustomFieldValue(newPerson, customFieldsCreated, customFieldValues, context);


        return newPerson;

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
            testPerson.setPersonUid(personDao.insert(testPerson));

            //Create ClazzMember
            ClazzMember clazzMember = new ClazzMember();
            clazzMember.setClazzMemberClazzUid(testClazz.getClazzUid());
            clazzMember.setRole(ClazzMember.ROLE_STUDENT);

            clazzMember.setClazzMemberPersonUid(testPerson.getPersonUid());
            clazzMember.setAttendancePercentage(personAttendancePercentage);
            clazzMemberDao.insert(clazzMember);

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
            newClazzLog.setClazzClazzUid(thisClazz.getClazzUid());
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
                    ClazzListPresenter.ARG_CLAZZ_UID + "=" + feedClazzUid + "&" +
                    ClazzLogDetailPresenter.ARG_LOGDATE + "=" + feedLogDate;
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
