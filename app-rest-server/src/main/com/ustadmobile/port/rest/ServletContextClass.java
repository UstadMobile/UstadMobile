package com.ustadmobile.port.rest;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.PersonCustomFieldDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonDetailPresenterFieldDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField;
import com.ustadmobile.lib.db.entities.PersonField;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.CUSTOM_FIELD_MIN_UID;

public class ServletContextClass implements ServletContextListener
    {

        public String dummyBaseUrl = "http://localhost/dummy/address/";
        @Override
        public void contextDestroyed(ServletContextEvent arg0) {
            System.out.println("ServletContextListener destroyed");
        }

        //Run this before web application is started
        @Override
        public void contextInitialized(ServletContextEvent arg0) {
            System.out.println("ServletContextListener started");

            //Creating admin
            UmAppDatabase repository = UmAppDatabase.getInstance(arg0.getServletContext());
            PersonDao dao =
                    repository.getRepository(dummyBaseUrl, "dummy")
                            .getPersonDao();
            dao.createAdmin();

            //Adding stuff
            addFieldData(arg0);

            System.out.println("done setup");

        }

        /**
         * Adds dummy data in the start of the application here. It also sets a key so that we don't
         * add the dummy data every time. This will get replaced with real data that will sync with
         * the server.
         */
        public void addFieldData(ServletContextEvent arg0){

            UmAppDatabase repository = UmAppDatabase.getInstance(arg0.getServletContext());

            List<HeadersAndFields> allFields = getAllFields();

            //Create Custom Fields:
            PersonCustomFieldDao personCustomFieldDao =
                    repository.getRepository(dummyBaseUrl, "dmmy")
                            .getPersonCustomFieldDao();
            PersonDetailPresenterFieldDao personDetailPresenterFieldDao =
                    repository.getRepository(dummyBaseUrl,"dmmy")
                            .getPersonDetailPresenterFieldDao();

            System.out.println("STARTING PERSIST");


            for (HeadersAndFields field: allFields){
                boolean isHeader = false;
                if(field.fieldType == PersonField.FIELD_TYPE_HEADER){
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
                    if(field.fieldUid == 0){
                        int lastPersonCustomFieldUidUsed = personCustomFieldDao.findLatestUid();
                        int newCustomPersonCustomFieldUid = lastPersonCustomFieldUidUsed + 1;
                        if(lastPersonCustomFieldUidUsed < CUSTOM_FIELD_MIN_UID){
                            //first Custom field
                            newCustomPersonCustomFieldUid =
                                    CUSTOM_FIELD_MIN_UID + 1;
                        }
                        pcf1.setPersonCustomFieldUid(newCustomPersonCustomFieldUid);
                        field.fieldUid = newCustomPersonCustomFieldUid;
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
                Long pdpf1Uid = personDetailPresenterFieldDao.insert(pdpf1);
                pdpf1.setPersonDetailPresenterFieldUid(pdpf1Uid);
            }

            //Set that we have created dummy data so that check for this and don't create it again.



        }

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


        public List<HeadersAndFields> getAllFields(){

            List<HeadersAndFields> allFields = new ArrayList<>();



            allFields.add(new HeadersAndFields(
                    "",
                    "",
                    0,
                    0,
                    1,
                    PersonField.FIELD_TYPE_HEADER,
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
                    PersonField.FIELD_TYPE_HEADER,
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
                    PersonField.FIELD_TYPE_HEADER,
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
                    PersonField.FIELD_TYPE_HEADER,
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

            return allFields;
        }
}