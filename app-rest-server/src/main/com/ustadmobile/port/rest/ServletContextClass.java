package com.ustadmobile.port.rest;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.EntityRoleDao;
import com.ustadmobile.core.db.dao.PersonAuthDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonDetailPresenterFieldDao;
import com.ustadmobile.core.db.dao.PersonGroupDao;
import com.ustadmobile.core.db.dao.PersonGroupMemberDao;
import com.ustadmobile.core.db.dao.RoleDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionDao;
import com.ustadmobile.core.db.dao.SocialNominationQuestionSetDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;
import com.ustadmobile.lib.db.entities.PersonDetailPresenterField;
import com.ustadmobile.lib.db.entities.PersonField;
import com.ustadmobile.lib.db.entities.PersonGroup;
import com.ustadmobile.lib.db.entities.PersonGroupMember;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.entities.SocialNominationQuestion;
import com.ustadmobile.lib.db.entities.SocialNominationQuestionSet;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import static com.ustadmobile.lib.db.entities.PersonDetailPresenterField.CUSTOM_FIELD_MIN_UID;
import static com.ustadmobile.lib.db.entities.Role.ROLE_NAME_MNE;
import static com.ustadmobile.lib.db.entities.Role.ROLE_NAME_OFFICER;
import static com.ustadmobile.lib.db.entities.Role.ROLE_NAME_SEL;
import static com.ustadmobile.lib.db.entities.Role.ROLE_NAME_SITE_STAFF;
import static com.ustadmobile.lib.db.entities.Role.ROLE_NAME_TEACHER;

public class ServletContextClass implements ServletContextListener
    {

        public String dummyBaseUrl = "http://localhost/dummy/address/";
        public String dummyAuth = "dummy";
        UmAppDatabase appDb;
        UmAppDatabase appDbRepository;

        PersonCustomFieldDao personCustomFieldDao;
        PersonDetailPresenterFieldDao personDetailPresenterFieldDao;
        PersonDao personDao;
        RoleDao roleDao;
        EntityRoleDao entityRoleDao;
        PersonAuthDao personAuthDao;
        PersonGroupDao personGroupDao;
        PersonGroupMemberDao personGroupMemberDao;

        Role officerRole, selRole;

        private int fieldIndex = 0;
        List<HeadersAndFields> allFields;

        @Override
        public void contextDestroyed(ServletContextEvent arg0) {
            System.out.println("ServletContextListener destroyed");
        }

        //Run this before web application is started
        @Override
        public void contextInitialized(ServletContextEvent evt) {
            System.out.println("ServletContextListener started");

            appDb = UmAppDatabase.getInstance(evt.getServletContext());
            appDb.setAttachmentsDir(evt.getServletContext().getRealPath("/WEB-INF/attachments/"));
            System.out.println("Set db attachments path to: " + appDb.getAttachmentsDir());

            appDbRepository = appDb.getRepository(dummyBaseUrl, dummyAuth);

            personCustomFieldDao =
                    appDbRepository.getPersonCustomFieldDao();
            personDetailPresenterFieldDao = appDb.getRepository(dummyBaseUrl, dummyAuth)
                    .getPersonDetailPresenterFieldDao();
            personDao = appDb.getRepository(dummyBaseUrl, dummyAuth).getPersonDao();
            roleDao = appDb.getRepository(dummyBaseUrl, dummyAuth).getRoleDao();
            entityRoleDao = appDb.getRepository(dummyBaseUrl, dummyAuth).getEntityRoleDao();
            personAuthDao = appDb.getRepository(dummyBaseUrl, dummyAuth).getPersonAuthDao();
            personGroupDao = appDb.getRepository(dummyBaseUrl,dummyAuth).getPersonGroupDao();
            personGroupMemberDao = appDb.getRepository(dummyBaseUrl, dummyAuth).getPersonGroupMemberDao();


            //Load initial data
            loadInitialData();


        }

        public void loadInitialData(){

            //Create Admin
            Person adminPerson = personDao.findByUsername("admin");
            if(adminPerson == null) {
                adminPerson = new Person();
                adminPerson.setAdmin(true);
                adminPerson.setUsername("admin");
                adminPerson.setFirstNames("Admin");
                adminPerson.setLastName("Admin");

                adminPerson.setPersonUid(personDao.insert(adminPerson));

                PersonAuth adminPersonAuth = new PersonAuth(adminPerson.getPersonUid(),
                        PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                                PersonAuthDao.encryptPassword("irZahle2"));
                personAuthDao.insertAsync(adminPersonAuth, new UmCallback<Long>() {
                    @Override
                    public void onSuccess(Long result) {
                        //Admin created.
                        System.out.println("ServletContextClass: Admin created. Continuing..");
                        addRolesAndPermissions();
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });

            }else {
                System.out.println("ServletContextClass: Admin Already created. Continuing..");
                addRolesAndPermissions();
            }


        }


        public void createSELOfficer(){

            Person selPerson = personDao.findByUsername("sel");
            if(selPerson == null){
                selPerson = new Person();
                selPerson.setActive(true);
                selPerson.setUsername("sel");
                selPerson.setFirstNames("SEL");
                selPerson.setLastName("SEL");

                personDao.createPersonWithGroupAsync(selPerson, new UmCallback<PersonDao.PersonWithGroup>() {
                    @Override
                    public void onSuccess(PersonDao.PersonWithGroup personWithGroup) {
                        long selPersonUid = personWithGroup.getPersonUid();
                        if(personWithGroup != null){

                            //Create password
                            PersonAuth selPersonAuth = new PersonAuth(selPersonUid,
                                    PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                                            PersonAuthDao.encryptPassword("irZahle4"));
                            personAuthDao.insertAsync(selPersonAuth, new UmCallback<Long>() {
                                @Override
                                public void onSuccess(Long result) {

                                    if(result != null) {

                                        //Create entity roles for all clazzes
                                        //Assign Role for all clazzes

                                        System.out.println("Looping over classes..");
                                        List<Clazz> allClazzes = personDao.findAllClazzes();
                                        for (Clazz thisClazz : allClazzes) {
                                            EntityRole entityRole = new EntityRole();
                                            entityRole.setErRoleUid(selRole.getRoleUid());
                                            entityRole.setErTableId(Clazz.TABLE_ID);
                                            entityRole.setErGroupUid(personWithGroup.getPersonGroupUid());
                                            entityRole.setErEntityUid(thisClazz.getClazzUid());
                                            entityRoleDao.insert(entityRole);
                                        }

                                    }else{
                                        System.out.println("ServletContextClass: Unable to set auth");
                                    }

                                    //Adding field data:
                                    addFieldData();
                                }

                                @Override
                                public void onFailure(Throwable exception) {
                                    exception.printStackTrace();
                                }
                            });

                        }else{
                            System.out.println("ServletContextClass: ERROR createPersonWithGroupAsync could not create Person with Person Group. ERROR");
                        }

                        //Adding stuff
                        addFieldData();

                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });


            }else{
                System.out.println("SEL already created. Updating permissions over all classes");
                personGroupMemberDao.findAllGroupWherePersonIsIn(selPerson.getPersonUid(),
                        new UmCallback<List<PersonGroupMember>>() {
                            @Override
                            public void onSuccess(List<PersonGroupMember> result) {

                                if(!result.isEmpty()) {

                                    Long selGroupUid = result.get(0).getGroupMemberGroupUid();

                                    List<Clazz> allClazzes = personDao.findAllClazzes();
                                    for (Clazz thisClazz : allClazzes) {

                                        entityRoleDao.findByEntitiyAndPersonGroup(Clazz.TABLE_ID, thisClazz.getClazzUid(),
                                                selGroupUid, new UmCallback<List<EntityRole>>() {
                                                    @Override
                                                    public void onSuccess(List<EntityRole> existingER) {
                                                        if (existingER.isEmpty()) {
                                                            EntityRole entityRole = new EntityRole();
                                                            entityRole.setErRoleUid(selRole.getRoleUid());
                                                            entityRole.setErTableId(Clazz.TABLE_ID);
                                                            entityRole.setErGroupUid(selGroupUid);
                                                            entityRole.setErEntityUid(thisClazz.getClazzUid());
                                                            entityRoleDao.insert(entityRole);
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Throwable exception) {
                                                        exception.printStackTrace();
                                                    }
                                                });
                                    }
                                }else{
                                    System.out.println("ServletContxtClass: ERROR Unable to find Person Group. ERROR");
                                }


                                //Adding stuff
                                addFieldData();


                            }

                            @Override
                            public void onFailure(Throwable exception) {
                                exception.printStackTrace();
                            }
                        });

            }
        }


        public void createOfficer(){

            Person officerPerson = personDao.findByUsername("officer");
            if(officerPerson == null){
                officerPerson = new Person();
                officerPerson.setActive(true);
                officerPerson.setUsername("officer");
                officerPerson.setFirstNames("Officer");
                officerPerson.setLastName("Officer");

                personDao.createPersonWithGroupAsync(officerPerson, new UmCallback<PersonDao.PersonWithGroup>() {
                    @Override
                    public void onSuccess(PersonDao.PersonWithGroup personWithGroup) {
                        long officerPersonUid = personWithGroup.getPersonUid();
                        if(personWithGroup != null){

                            //Create password
                            PersonAuth officerPersonAuth = new PersonAuth(officerPersonUid,
                                    PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                                            PersonAuthDao.encryptPassword("irZahle3"));
                            personAuthDao.insertAsync(officerPersonAuth, new UmCallback<Long>() {
                                @Override
                                public void onSuccess(Long result) {

                                    if(result != null) {

                                        //Create entity roles for all clazzes
                                        //Assign Role for all clazzes

                                        System.out.println("Looping over classes..");
                                        List<Clazz> allClazzes = personDao.findAllClazzes();
                                        for (Clazz thisClazz : allClazzes) {
                                            EntityRole entityRole = new EntityRole();
                                            entityRole.setErRoleUid(officerRole.getRoleUid());
                                            entityRole.setErTableId(Clazz.TABLE_ID);
                                            entityRole.setErGroupUid(personWithGroup.getPersonGroupUid());
                                            entityRole.setErEntityUid(thisClazz.getClazzUid());
                                            entityRoleDao.insert(entityRole);
                                        }

                                    }else{
                                        System.out.println("ServletContextClass: Unable to set auth");
                                    }


                                    //Adding SEL data:
                                    createSELOfficer();
                                }

                                @Override
                                public void onFailure(Throwable exception) {
                                    exception.printStackTrace();
                                }
                            });

                        }else{
                            System.out.println("ServletContextClass: ERROR createPersonWithGroupAsync could not create Person with Person Group. ERROR");
                        }

                        createSELOfficer();

                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });


            }else{
                System.out.println("Officer already created. Updating permissions over all classes");
                personGroupMemberDao.findAllGroupWherePersonIsIn(officerPerson.getPersonUid(),
                        new UmCallback<List<PersonGroupMember>>() {
                    @Override
                    public void onSuccess(List<PersonGroupMember> result) {

                        if(!result.isEmpty()) {

                            Long officerGroupUid = result.get(0).getGroupMemberGroupUid();

                            List<Clazz> allClazzes = personDao.findAllClazzes();
                            for (Clazz thisClazz : allClazzes) {

                                entityRoleDao.findByEntitiyAndPersonGroup(Clazz.TABLE_ID, thisClazz.getClazzUid(),
                                    officerGroupUid, new UmCallback<List<EntityRole>>() {
                                    @Override
                                    public void onSuccess(List<EntityRole> existingER) {
                                        if (existingER.isEmpty()) {
                                            EntityRole entityRole = new EntityRole();
                                            entityRole.setErRoleUid(officerRole.getRoleUid());
                                            entityRole.setErTableId(Clazz.TABLE_ID);
                                            entityRole.setErGroupUid(officerGroupUid);
                                            entityRole.setErEntityUid(thisClazz.getClazzUid());
                                            entityRoleDao.insert(entityRole);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Throwable exception) {
                                        exception.printStackTrace();
                                    }
                                });
                            }
                        }else{
                            System.out.println("ServletContxtClass: ERROR Unable to find Person Group. ERROR");
                        }

                        //Go next to SEL
                        createSELOfficer();


                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });

            }
        }

        public class TestRole{
            String username;
            String password;

            TestRole(String username, String password){
                this.username = username;
                this.password = password;
            }
        }


        public void addRolesAndPermissions(){

            System.out.println("Adding roles and permissions");


            //TEACHER
            roleDao.findByName(ROLE_NAME_TEACHER, new UmCallback<Role>() {
                @Override
                public void onSuccess(Role result) {
                    if(result == null){
                        //Add teacher role
                        Role newRole = new Role();

                        newRole.setRoleName(ROLE_NAME_TEACHER);
                        long teacherPermissions =
                            Role.PERMISSION_CLAZZ_ADD_STUDENT |
                            Role.PERMISSION_CLAZZ_SELECT |                  //See Clazzes
                            Role.PERMISSION_CLAZZ_UPDATE |                  //Update Clazz
                            Role.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT |     //See Clazz Activity
                            Role.PERMISSION_CLAZZ_LOG_ACTIVITY_UPDATE |     //Update Clazz Activity
                            Role.PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT |     //Add/Take Clazz Activities
                            Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT |   //See Attendance
                            Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT |   //Take attendance
                            Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE |   //Update attendance
                            Role.PERMISSION_PERSON_SELECT  |                //See People
                            Role.PERMISSION_PERSON_PICTURE_INSERT |         //Insert Person Picture
                            Role.PERMISSION_PERSON_PICTURE_SELECT |         //See Person Picture
                            Role.PERMISSION_PERSON_PICTURE_UPDATE           //Update Person picture
                            ;
                        newRole.setRolePermissions(teacherPermissions);
                        Long newRoleUid = roleDao.insert(newRole);
                    }else{
                        System.out.println("Role already created for teacher");
                    }


                    //Officer
                    roleDao.findByName(ROLE_NAME_OFFICER, new UmCallback<Role>() {
                        @Override
                        public void onSuccess(Role result) {
                            if(result == null){
                                Role newRole = new Role();
                                newRole.setRoleName(ROLE_NAME_OFFICER);
                                long officerPermissions =
                                    Role.PERMISSION_CLAZZ_ADD_STUDENT |
                                            Role.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT |
                                            Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT |
                                            Role.PERMISSION_CLAZZ_SELECT |
                                            Role.PERMISSION_CLAZZ_UPDATE |
                                            Role.PERMISSION_PERSON_SELECT |
                                            Role.PERMISSION_PERSON_UPDATE |
                                            Role.PERMISSION_PERSON_INSERT |
                                            Role.PERMISSION_PERSON_PICTURE_INSERT |
                                            Role.PERMISSION_PERSON_PICTURE_SELECT |
                                            Role.PERMISSION_PERSON_PICTURE_UPDATE

                                    ;
                                newRole.setRolePermissions(officerPermissions);
                                newRole.setRoleUid(roleDao.insert(newRole));

                                officerRole = newRole;


                            }else{
                                officerRole = result;
                                System.out.println("Role already created for officer");
                            }

                            //SEL
                            roleDao.findByName(ROLE_NAME_SEL, new UmCallback<Role>() {
                                @Override
                                public void onSuccess(Role result) {
                                    if(result == null){
                                        Role newRole = new Role();
                                        newRole.setRoleName(ROLE_NAME_SEL);
                                        long selPermissions =
                                                Role.PERMISSION_CLAZZ_SELECT |
                                                        Role.PERMISSION_PERSON_SELECT |
                                                        Role.PERMISSION_PERSON_PICTURE_SELECT |
                                                        Role.PERMISSION_SEL_QUESTION_SELECT |
                                                        Role.PERMISSION_SEL_QUESTION_RESPONSE_SELECT |
                                                        Role.PERMISSION_SEL_QUESTION_RESPONSE_INSERT |
                                                        Role.PERMISSION_SEL_QUESTION_RESPONSE_UPDATE
                                                ;
                                        newRole.setRolePermissions(selPermissions);
                                        roleDao.insert(newRole);
                                        selRole = newRole;
                                    }else{
                                        selRole = result;
                                        System.out.println("Role already created for SEL");
                                    }

                                    //MNE
                                    roleDao.findByName(ROLE_NAME_MNE, new UmCallback<Role>() {
                                        @Override
                                        public void onSuccess(Role result) {
                                            if(result == null){
                                                Role newRole = new Role();
                                                newRole.setRoleName(ROLE_NAME_MNE);
                                                long mnePermissions =
                                                        Role.PERMISSION_CLAZZ_ADD_STUDENT |
                                                                Role.PERMISSION_CLAZZ_LOG_ACTIVITY_SELECT |
                                                                Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT |
                                                                Role.PERMISSION_CLAZZ_SELECT |
                                                                Role.PERMISSION_CLAZZ_UPDATE |
                                                                Role.PERMISSION_PERSON_SELECT |
                                                                Role.PERMISSION_PERSON_UPDATE |
                                                                Role.PERMISSION_PERSON_INSERT |
                                                                Role.PERMISSION_PERSON_PICTURE_INSERT |
                                                                Role.PERMISSION_PERSON_PICTURE_SELECT |
                                                                Role.PERMISSION_PERSON_PICTURE_UPDATE |
                                                                Role.PERMISSION_SEL_QUESTION_INSERT |
                                                                Role.PERMISSION_SEL_QUESTION_UPDATE |
                                                                Role.PERMISSION_SEL_QUESTION_SELECT |
                                                                Role.PERMISSION_SEL_QUESTION_RESPONSE_SELECT |
                                                                Role.PERMISSION_REPORTS_VIEW
                                                        ;
                                                newRole.setRolePermissions(mnePermissions);
                                                roleDao.insert(newRole);
                                            }else{
                                                System.out.println("Role already created for MNE");
                                            }

                                            //SITE STAFF
                                            roleDao.findByName(ROLE_NAME_SITE_STAFF, new UmCallback<Role>() {
                                                @Override
                                                public void onSuccess(Role result) {
                                                    if(result == null){
                                                        Role newRole = new Role();
                                                        newRole.setRoleName(ROLE_NAME_SITE_STAFF);
                                                        long siteStaffPermissions =
                                                                Role.PERMISSION_PERSON_SELECT |
                                                                        Role.PERMISSION_PERSON_PICTURE_SELECT
                                                                ;
                                                        newRole.setRolePermissions(siteStaffPermissions);
                                                        roleDao.insert(newRole);
                                                    }else{
                                                        System.out.println("Role already created for Site Staff");
                                                    }

                                                    System.out.println("ServletContextClass: Checked all Rols and Permissions. Continuing..");
                                                    createOfficer();
                                                }

                                                @Override
                                                public void onFailure(Throwable exception) {
                                                    exception.printStackTrace();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFailure(Throwable exception) {
                                            exception.printStackTrace();
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Throwable exception) {
                                    exception.printStackTrace();
                                }
                            });
                        }

                        @Override
                        public void onFailure(Throwable exception) {
                            exception.printStackTrace();
                        }
                    });
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });


        }


        public void addSELQuestions(){

            System.out.println("ServletContextClass: Adding SEL Questions: TODO: REMOVE ME");

            String question1Text = "Who sits next to you?";
            String question2Text = "Who participates a lot in class?";
            String question3Text = "Who is disruptive during class?";
            String question4Text = "Who are your friends in class?";
            String question5Text = "Who are the kids you spend time with outside of class?";

            //Create SEL questions :
            SocialNominationQuestionSetDao questionSetDao =
                    appDb.getRepository(dummyBaseUrl, dummyAuth).getSocialNominationQuestionSetDao();

            SocialNominationQuestionSet questionSet = new SocialNominationQuestionSet();
            questionSet.setTitle("Default set");
            Long questionSetUid = questionSetDao.insert(questionSet);
            questionSet.setSocialNominationQuestionSetUid(questionSetUid);
            System.out.println("Question set uid: " + questionSetUid);

            SocialNominationQuestionDao questionDao = appDb.getRepository(dummyBaseUrl, dummyAuth)
                    .getSocialNominationQuestionDao();

            SocialNominationQuestion question1 = new SocialNominationQuestion();
            question1.setSocialNominationQuestionSocialNominationQuestionSetUid(
                    questionSetUid);
            question1.setQuestionIndex(1);
            question1.setQuestionText(question1Text);
            question1.setMultiNominations(true);
            question1.setAssignToAllClasses(true);
            questionDao.findByQuestionStringAsync(question1Text, new UmCallback<List<SocialNominationQuestion>>() {
                @Override
                public void onSuccess(List<SocialNominationQuestion> result) {
                    if(result != null && result.size() == 1){
                        //skip
                    }else if(result == null || result.size() == 0){
                        System.out.println("Persisting 1:  ");
                        questionDao.insert(question1);
                    }
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });


            SocialNominationQuestion question2 = new SocialNominationQuestion();
            question2.setSocialNominationQuestionSocialNominationQuestionSetUid(
                    questionSetUid);
            question2.setQuestionIndex(2);
            question2.setQuestionText(question2Text);
            question2.setMultiNominations(true);
            question2.setAssignToAllClasses(true);
            questionDao.findByQuestionStringAsync(question2Text, new UmCallback<List<SocialNominationQuestion>>() {
                @Override
                public void onSuccess(List<SocialNominationQuestion> result) {
                    if(result != null && result.size() == 1){
                        //skip
                    }else if(result == null || result.size() == 0){
                        System.out.println("Persisting 2:  ");
                        questionDao.insert(question2);
                    }
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });

            SocialNominationQuestion question3 = new SocialNominationQuestion();
            question3.setSocialNominationQuestionSocialNominationQuestionSetUid(
                    questionSetUid);
            question3.setQuestionIndex(3);
            question3.setQuestionText(question3Text);
            question3.setMultiNominations(true);
            question3.setAssignToAllClasses(true);
            questionDao.findByQuestionStringAsync(question3Text, new UmCallback<List<SocialNominationQuestion>>() {
                @Override
                public void onSuccess(List<SocialNominationQuestion> result) {
                    if(result != null && result.size() == 1){
                        //skip
                    }else if(result == null || result.size() == 0){
                        System.out.println("Persisting 3:  ");
                        questionDao.insert(question3);
                    }
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });

            SocialNominationQuestion question4 = new SocialNominationQuestion();
            question4.setSocialNominationQuestionSocialNominationQuestionSetUid(
                    questionSetUid);
            question4.setQuestionIndex(4);
            question4.setQuestionText(question4Text);
            question4.setMultiNominations(true);
            question4.setAssignToAllClasses(true);
            questionDao.findByQuestionStringAsync(question4Text, new UmCallback<List<SocialNominationQuestion>>() {
                @Override
                public void onSuccess(List<SocialNominationQuestion> result) {
                    if(result != null && result.size() == 1){
                        //skip
                    }else if(result == null || result.size() == 0){
                        System.out.println("Persisting 4:  ");
                        questionDao.insert(question4);
                    }
                }

                @Override
                public void onFailure(Throwable exception) {

                }
            });

            SocialNominationQuestion question5 = new SocialNominationQuestion();
            question5.setSocialNominationQuestionSocialNominationQuestionSetUid(
                    questionSetUid);
            question5.setQuestionIndex(5);
            question5.setQuestionText(question5Text);
            question5.setMultiNominations(true);
            question5.setAssignToAllClasses(true);
            questionDao.findByQuestionStringAsync(question5Text, new UmCallback<List<SocialNominationQuestion>>() {
                @Override
                public void onSuccess(List<SocialNominationQuestion> result) {
                    if(result != null && result.size() == 1){
                        //skip
                    }else if(result == null || result.size() == 0){
                        System.out.println("Persisting 5:  ");
                        questionDao.insert(question5);
                    }
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }

        public void addNextField(){

            if(fieldIndex == allFields.size()){
                return;
            }

            HeadersAndFields field = allFields.get(fieldIndex);

            boolean isHeader = false;
            if(field.fieldType == PersonField.FIELD_TYPE_HEADER){
                isHeader = true;
            }

            boolean finalIsHeader = isHeader;
            personCustomFieldDao.findByFieldNameAsync(field.fieldName,
            new UmCallback<List<PersonField>>() {
                @Override
                public void onSuccess(List<PersonField> resultList) {

                    //Create the custom fields - basically label & icon .
                    PersonField personField = new PersonField();

                    if (resultList.isEmpty()) {

                        //Create the field only if it is a field (ie not a header)
                        if (!finalIsHeader) {
                            personField.setFieldIcon(field.fieldIcon); //Icon
                            personField.setFieldName(field.fieldName); //Internal name
                            personField.setLabelMessageId(field.fieldLabel);    //Label

                            //Set PersonFields' Uid (PersonCustomFieldUid) (No auto generation)
                            //If field not set ie its a Custom Field
                            if(field.fieldUid == 0){
                                //It is a custom field
                                int lastPersonCustomFieldUidUsed =
                                        personCustomFieldDao.findLatestUid();
                                int newCustomPersonCustomFieldUid =
                                        lastPersonCustomFieldUidUsed + 1;
                                if(lastPersonCustomFieldUidUsed < CUSTOM_FIELD_MIN_UID){
                                    //first Custom field
                                    newCustomPersonCustomFieldUid = CUSTOM_FIELD_MIN_UID + 1;
                                }
                                personField.setPersonCustomFieldUid(newCustomPersonCustomFieldUid);
                                field.fieldUid = newCustomPersonCustomFieldUid;

                            }else {
                                //Not a custom field.
                                personField.setPersonCustomFieldUid(field.fieldUid);   //Field's uid
                            }

                            System.out.println("Field: " + field.fieldName +
                                    " Field uid: " + field.fieldUid);

                            //Persist
                            personCustomFieldDao.insertAsync(personField, new UmCallback<Long>() {
                                @Override
                                public void onSuccess(Long result) {
                                    //Persist 2
                                    createPersonDetailPresenterField(field, finalIsHeader, personField,
                                            personDetailPresenterFieldDao, true);
                                }

                                @Override
                                public void onFailure(Throwable exception) {

                                }
                            });

                        }





                    } else {

                        System.out.println("Already created 1 (" + field.fieldName + "). skipping..");
                    }
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });

        }


        /**
         * Adds dummy data in the start of the application here. It also sets a key so that we don't
         * add the dummy data every time. This will get replaced with real data that will sync with
         * the server.
         */
        public void addFieldData(){
            System.out.println("ServletContextClass: Adding Field Data");
            allFields = getAllFields();

            //Start with next field (1st field really)
            addNextField();

        }

        public void createPersonDetailPresenterField (HeadersAndFields field, boolean isHeader,
                                      PersonField pcf,
                                      PersonDetailPresenterFieldDao personDetailPresenterFieldDao,
                                                      Boolean gotoNext){


            personDetailPresenterFieldDao.findAllByFieldIndex(field.fieldIndex,
                new UmCallback<List<PersonDetailPresenterField>>() {
                @Override
                public void onSuccess(List<PersonDetailPresenterField> resultList2) {

                    if (resultList2.isEmpty()) {

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
                            Long pcfUid = pcf.getPersonCustomFieldUid();
                            pdpf1.setFieldUid(pcfUid);
                        }else {
                            pdpf1.setHeaderMessageId(field.headerMessageId);
                        }

                        //persist:
                        personDetailPresenterFieldDao.insert(pdpf1);
                    } else {
                        System.out.println("Already created 2 (" + field.fieldIndex + ":" +
                                field.fieldLabel + "). skipping..");
                    }

                    if(gotoNext){
                        fieldIndex++;
                        addNextField();
                    }

                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });

        }

        /**
         * Just a POJO for this test class to loop through and create the fields.
         */
        class HeadersAndFields {

            public HeadersAndFields(String fieldIcon, String fieldName, int fieldLabel, int fieldUid,
                                    int fieldIndex, int fieldType, int headerMessageId,
                                    boolean readOnly, boolean viewMode, boolean editMode){

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

            List<HeadersAndFields> allTheFields = new ArrayList<>();


            allTheFields.add(new HeadersAndFields(
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
            allTheFields.add(new HeadersAndFields(
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
            allTheFields.add(new HeadersAndFields(
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
            allTheFields.add(new HeadersAndFields(
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
            allTheFields.add(new HeadersAndFields(
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
            allTheFields.add(new HeadersAndFields(
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
            allTheFields.add(new HeadersAndFields(
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
            allTheFields.add(new HeadersAndFields(
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
            allTheFields.add(new HeadersAndFields(
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
            allTheFields.add(new HeadersAndFields(
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
            allTheFields.add(new HeadersAndFields(
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
            allTheFields.add(new HeadersAndFields(
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
            allTheFields.add(new HeadersAndFields(
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
            allTheFields.add(new HeadersAndFields(
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
            allTheFields.add(new HeadersAndFields(
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
            allTheFields.add(new HeadersAndFields(
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

            allTheFields.add(cf1);
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
            allTheFields.add(cf2);

            return allTheFields;
        }
}