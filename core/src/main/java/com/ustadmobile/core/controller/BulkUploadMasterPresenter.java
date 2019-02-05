package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonGroupDao;
import com.ustadmobile.core.db.dao.PersonGroupMemberDao;
import com.ustadmobile.core.db.dao.RoleDao;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.view.BulkUploadMasterView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonField;
import com.ustadmobile.lib.db.entities.PersonGroupMember;
import com.ustadmobile.lib.db.entities.Role;

import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

public class BulkUploadMasterPresenter extends UstadBaseController<BulkUploadMasterView> {

    public BulkUploadMasterPresenter(Object context, Hashtable arguments,
                                     BulkUploadMasterView view) {
        super(context, arguments, view);
    }

    private int currentPosition = 0;
    private List<String> lines;
    private long teacherRoleUid = 0L;

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);


    public void processNextLine(){
        currentPosition ++;
        System.out.println("BULK UPLOAD LINE : " + currentPosition);

        view.updateProgressValue(currentPosition, lines.size());

        if(currentPosition >= lines.size()){
            view.finish();
        }else {
            parseMasterListLineToDatabase(lines.get(currentPosition));
        }

    }

    public void processLocations(BulkUploadLine bulkLine){

        String location1Title = bulkLine.location1;
        String location2Title = bulkLine.location2;
        String location3Title = bulkLine.location3;
        String locationLeafTitle = bulkLine.locationLeaf;

        Location location1 = new Location();
        location1.setTitle(location1Title);

        Location location2 = new Location();
        location2.setTitle(location2Title);

        Location location3 = new Location();
        location3.setTitle(location3Title);

        Location locationLeaf = new Location();
        locationLeaf.setTitle(locationLeafTitle);

        LocationDao locationDao = repository.getLocationDao();

        locationDao.findByTitleAsync(location1Title, new UmCallback<List<Location>>() {
            @Override
            public void onSuccess(List<Location> location1s) {

                boolean move = false;
                if(location1s.size() == 0){

                    //Location not created. Create it.
                    location1.setLocationUid(locationDao.insert(location1));
                    move = true;
                }else if(location1s.size() == 1){

                    //Location already exists. Getting uid.
                    location1.setLocationUid(location1s.get(0).getLocationUid());
                    move = true;
                }else{
                    System.out.println("ERROR: Location: 1st level: More than 1 with the same title");
                }

                if(move)
                    //Move on to the 2nd level
                    locationDao.findByTitleAsync(location2Title, new UmCallback<List<Location>>() {
                        @Override
                        public void onSuccess(List<Location> location2s) {
                            boolean move = false;
                            if(location2s.size() ==0){
                                move = true;
                                //Location not created. Create it.
                                location2.setParentLocationUid(location1.getLocationUid());
                                location2.setLocationUid(locationDao.insert(location2));
                            }else if(location2s.size() == 1){
                                move = true;
                                //Location already exists. Getting uid.
                                location2.setLocationUid(location2s.get(0).getLocationUid());
                            }else{
                                System.out.println("ERROR: Location: 2nd level: More than 1 with the same title");
                            }

                            if(move)
                                //Move on to the 3rd level
                                locationDao.findByTitleAsync(location3Title, new UmCallback<List<Location>>() {
                                    @Override
                                    public void onSuccess(List<Location> location3s) {
                                        boolean move = false;
                                        if(location3s.size() == 0){
                                            move = true;
                                            location3.setParentLocationUid(location2.getLocationUid());
                                            location3.setLocationUid(locationDao.insert(location3));
                                        }else if(location3s.size() == 1){
                                            move = true;
                                            location3.setLocationUid(location3s.get(0).getLocationUid());
                                        }else{
                                            System.out.println("ERROR: Location: 3rd level: More than 1" +
                                                    " with the same title");
                                        }

                                        if(move)
                                            //Move on the the leaf level
                                            locationDao.findByTitleAsync(locationLeafTitle, new UmCallback<List<Location>>() {
                                                @Override
                                                public void onSuccess(List<Location> locationLeafs) {

                                                    boolean moveOn = false;
                                                    if(locationLeafs.size() == 0){
                                                        //Location leaf not created. Setting parent and
                                                        //persisting to get uid.
                                                        locationLeaf.setParentLocationUid(location3.getLocationUid());
                                                        locationLeaf.setLocationUid(locationDao.insert(locationLeaf));
                                                        moveOn = true;

                                                    }else if(locationLeafs.size() == 1){
                                                        //Location leaf already exists. Getting uid
                                                        locationLeaf.setLocationUid(locationLeafs.get(0).getLocationUid());
                                                        moveOn = true;
                                                    }else{
                                                        System.out.println("ERROR: Location Leaf: More than " +
                                                                "1 location leaf with the same title");
                                                    }

                                                    //Moving forward
                                                    if(moveOn)
                                                        processClazz(bulkLine, locationLeaf);
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

    public void processClazz(BulkUploadLine bulkLine, Location locationLeaf){
        //2. Class
        String clazzName = bulkLine.class_name;
        String clazzLocation = bulkLine.class_location;

        //Get clazz by name:
        repository.getClazzDao().findByClazzNameAsync(clazzName,
                new UmCallback<List<Clazz>>() {
                    @Override
                    public void onSuccess(List<Clazz> clazzes) {
                        boolean move = false;
                        Clazz thisClazz;
                        if(clazzes.size() == 0){   //No clazzes with that name.
                            //Create clazz
                            thisClazz = new Clazz();
                            thisClazz.setClazzName(clazzName);

                            repository.getClazzDao().insertAsync(thisClazz, new UmCallback<Long>() {
                                @Override
                                public void onSuccess(Long newClazzUid) {
                                    thisClazz.setClazzUid(newClazzUid);

                                    //Add location
                                    repository.getLocationDao()
                                            .findByTitleAsync(clazzLocation, new UmCallback<List<Location>>() {
                                                @Override
                                                public void onSuccess(List<Location> result) {
                                                    boolean move = false;
                                                    if (result.size() == 1) {
                                                        //Location exists and is unique
                                                        Location clazzLocation = result.get(0);
                                                        thisClazz.setClazzLocationUid(clazzLocation.getLocationUid());
                                                        thisClazz.setClazzActive(true);
                                                        thisClazz.setClazzLocationUid(locationLeaf.getLocationUid());
                                                        move = true;

                                                        repository.getClazzDao().updateAsync(thisClazz, new UmCallback<Integer>() {
                                                            @Override
                                                            public void onSuccess(Integer result) {
                                                                if(result != null) {
                                                                    //thisClazz already has uid set

                                                                    //Move on
                                                                    checkPerson(thisClazz, bulkLine, ClazzMember.ROLE_TEACHER);
                                                                }
                                                            }

                                                            @Override
                                                            public void onFailure(Throwable exception) {

                                                            }
                                                        });

                                                    }else{
                                                        //Location does not exist.
                                                        System.out.println("FAIL: LOCATION : " + clazzLocation
                                                                + " DOESN'T EXIST");
                                                    }

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



                        }else if (clazzes.size() > 1){   //Multiple clazzes with that name (ERROR)
                            System.out.println("ERROR : MULTIPLE CLAZZ WITH NAME: " + clazzName);
                        }else{
                            thisClazz = clazzes.get(0);
                            //Not updating clazz. Moving on
                            checkPerson(thisClazz, bulkLine, ClazzMember.ROLE_TEACHER);
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
    }

    public void parseMasterListLineToDatabase(String line){
        System.out.println("Processing line of length: " + line.length());

        BulkUploadLine bulkLine = new BulkUploadLine(line);

        //1. Location
        //2. Clazz
        //3. Teacher
        //4. Student

        getRole(bulkLine);

    }

    public void getRole(BulkUploadLine bulkLine){
        RoleDao roleDao = repository.getRoleDao();
        roleDao.findByName(Role.ROLE_NAME_TEACHER, new UmCallback<Role>() {
            @Override
            public void onSuccess(Role teacherRole) {
                if(teacherRole != null){
                    teacherRoleUid = teacherRole.getRoleUid();
                    processLocations(bulkLine);
                }else{
                    view.showMessage("Please wait until the app syncs and try again.");
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    public long getDOBFromString(String dateString){
        Locale currentLocale = Locale.getDefault();
        return UMCalendarUtil.getLongDateFromStringAndFormat(
                dateString,"dd/MM/yyyy", currentLocale);
    }



    public void checkClazzMember(Clazz thisClazz, BulkUploadLine bulkLine, long personPersonUid,
                                 int role){

        repository.getClazzMemberDao().findByPersonUidAndClazzUidAsync(
                personPersonUid, thisClazz.getClazzUid(),
                new UmCallback<ClazzMember>() {
                    @Override
                    public void onSuccess(ClazzMember clazzMember) {
                        if(clazzMember == null){
                            //Create one.
                            ClazzMember personClazzMember;
                            personClazzMember = new ClazzMember();
                            personClazzMember.setClazzMemberPersonUid(personPersonUid);
                            personClazzMember.setClazzMemberClazzUid(thisClazz.getClazzUid());
                            personClazzMember.setClazzMemberActive(true);
                            personClazzMember.setDateJoined(System.currentTimeMillis());
                            personClazzMember.setRole(role);

                            repository.getClazzMemberDao().insertAsync(personClazzMember,
                                    new UmCallback<Long>() {
                                        @Override
                                        public void onSuccess(Long clazzMemberUid) {
                                            if(clazzMemberUid != null) {
                                                if (role == ClazzMember.ROLE_TEACHER) {
                                                    checkPerson(thisClazz, bulkLine, ClazzMember.ROLE_STUDENT);
                                                }else{
                                                    processNextLine();

                                                }
                                            }else{
                                                System.out.println("ERROR: UNABLE TO PERSIST CLAZZMEMBER??");
                                            }
                                        }

                                        @Override
                                        public void onFailure(Throwable exception) {
                                            exception.printStackTrace();
                                        }
                                    });



                        }else{
                            //Exists already
                            if (role == ClazzMember.ROLE_TEACHER) {
                                checkPerson(thisClazz, bulkLine, ClazzMember.ROLE_STUDENT);
                            }else{
                                processNextLine();

                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable exception) {
                        exception.printStackTrace();
                    }
                }
        );
    }

    public void createEntityRoleForTeacherAndClazz(int role, Clazz thisClazz, long personPersonUid,
                                                   BulkUploadLine bulkLine){
        if(role == ClazzMember.ROLE_TEACHER){

            PersonDao personDao = repository.getPersonDao();
            PersonGroupDao personGroupDao = repository.getPersonGroupDao();
            PersonGroupMemberDao personGroupMemberDao = repository.getPersonGroupMemberDao();
            personGroupMemberDao.findAllGroupWherePersonIsIn(personPersonUid, new UmCallback<List<PersonGroupMember>>() {
                @Override
                public void onSuccess(List<PersonGroupMember> result) {

                    long personGroupUid;
                    if(result != null && result.size() > 0){
                        //Get parent group :  ASSUMING ITS THE FIRST ONE>>TODO>>ADD DATE TO PersonGroup
                        personGroupUid = result.get(0).getGroupMemberGroupUid();

                        //Create EntityRole
                        EntityRole entityRole = new EntityRole();
                        entityRole.setErTableId(Clazz.TABLE_ID);
                        entityRole.setErEntityUid(thisClazz.getClazzUid());
                        entityRole.setErRoleUid(teacherRoleUid);

                        //TODO: Add Check if already created or not
                        repository.getEntityRoleDao().findByEntitiyAndPersonGroup(
                                Clazz.TABLE_ID, thisClazz.getClazzUid(), personGroupUid,
                                new UmCallback<List<EntityRole>>() {
                                    @Override
                                    public void onSuccess(List<EntityRole> result) {
                                        if(result.isEmpty()){
                                            //Good, create one.

                                            repository.getEntityRoleDao().insertAsync(entityRole, new UmCallback<Long>() {
                                                @Override
                                                public void onSuccess(Long result) {
                                                    if(result != null){

                                                        //For a specific clazz
                                                        EntityRole newEntityClazzSpecific = new EntityRole();
                                                        newEntityClazzSpecific.setErGroupUid(personGroupUid);
                                                        newEntityClazzSpecific.setErRoleUid(teacherRoleUid);
                                                        newEntityClazzSpecific.setErTableId(Clazz.TABLE_ID);
                                                        newEntityClazzSpecific.setErEntityUid(thisClazz.getClazzUid());
                                                        repository.getEntityRoleDao()
                                                                .insertAsync(newEntityClazzSpecific, new UmCallback<Long>() {
                                                                    @Override
                                                                    public void onSuccess(Long entityRoleDaoUid) {
                                                                        if(entityRoleDaoUid != null){
                                                                            checkClazzMember(thisClazz, bulkLine,
                                                                                    personPersonUid, role);
                                                                        }else{
                                                                            view.showMessage("Something went wrong in clazz entity roles");
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onFailure(Throwable exception) {
                                                                        exception.printStackTrace();
                                                                    }
                                                                });

                                                    }else{
                                                        view.showMessage("Something went wrong");
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Throwable exception) {
                                                    exception.printStackTrace();
                                                }
                                            });

                                        }else{
                                            //Already created. continue.
                                            checkClazzMember(thisClazz, bulkLine, personPersonUid, role);
                                        }

                                    }

                                    @Override
                                    public void onFailure(Throwable exception) {
                                        exception.printStackTrace();
                                    }
                                }
                        );


                    }
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });

        }else{
            checkClazzMember(thisClazz, bulkLine, personPersonUid, role);
        }


    }

    public void checkPerson(Clazz thisClazz, BulkUploadLine bulkLine, int role){
        String teacherUsername = bulkLine.teacher_username;
        String teacherFirstName = bulkLine.teacher_first_name;
        String teacherLastName = bulkLine.teacher_last_name;
        String teacherPassword = bulkLine.teacher_password;
        String teacherId = bulkLine.teacher_id;
        String teacherPhoneNo = bulkLine.teacher_phone_no;

        String studentUsername = bulkLine.username;
        String studentPassword = bulkLine.password;
        String stuentFirstName = bulkLine.first_name;
        String studentLastName = bulkLine.last_name;
        String studentGender = bulkLine.gender;
        String studentMotherName = bulkLine.mother_name;
        String studentPhoneNo = bulkLine.phone_no;
        String studentSchool = bulkLine.school;
        String studentAddress = bulkLine.address;
        String studentFatherName = bulkLine.father_name;
        String studentDateOfBirth = bulkLine.dob;

        PersonCustomFieldDao personFieldDao = repository.getPersonCustomFieldDao();
        PersonCustomFieldValueDao personCustomFieldValueDao =
                repository.getPersonCustomFieldValueDao();

        String username;
        if(role == ClazzMember.ROLE_TEACHER){
            username = teacherUsername;
        }else{
            username = studentUsername;
        }

        repository.getPersonDao().findByUsernameAsync(username, new UmCallback<Person>() {

            @Override
            public void onSuccess(Person thePerson) {
                if(thePerson != null){
                    //person object exists

                    //Still create Entity Role for this different Clazz
                    // and //Check for ClazzMember then  directly
                    createEntityRoleForTeacherAndClazz(role,thisClazz, thePerson.getPersonUid(),
                            bulkLine);




                }else{
                    //Create new person
                    Person person = new Person();

                    if(role == ClazzMember.ROLE_TEACHER) {
                        person.setFirstNames(teacherFirstName);
                        person.setLastName(teacherLastName);
                        person.setPhoneNum(teacherPhoneNo);
                        person.setUsername(teacherUsername);

                        //TODO: Set teacher id
                        //TODO: Set teacher authentication:
                        PersonAuth personAuth = new PersonAuth();
                    }else{
                        person.setUsername(studentUsername);
                        person.setFirstNames(stuentFirstName);
                        person.setLastName(studentLastName);
                        person.setMotherName(studentMotherName);
                        person.setFatherName(studentFatherName);
                        person.setAddress(studentAddress);
                        person.setPhoneNum(studentPhoneNo);
                        person.setDateOfBirth(getDOBFromString(studentDateOfBirth));
                        if (studentGender.toLowerCase().startsWith("f")) {
                            person.setGender(Person.GENDER_FEMALE);
                        }else if(studentGender.toLowerCase().startsWith("m")){
                            person.setGender(Person.GENDER_MALE);
                        }

                        //TODO: Set student Id
                        //TODO: Set student authentication
                    }

                    person.setActive(true);

                    repository.getPersonDao().createPersonWithGroupAsync(person,
                            new UmCallback<PersonDao.PersonWithGroup>() {
                                @Override
                                public void onSuccess(PersonDao.PersonWithGroup personWithGroup) {
                                    Long personPersonUid = personWithGroup.getPersonUid();
                                    Long personGroupUid = personWithGroup.getPersonGroupUid();

                                    //TODO: Set student's custom field : school
                                    if(role == ClazzMember.ROLE_STUDENT) {
                                        personFieldDao.findByLabelMessageId(
                                                String.valueOf(MessageID.current_formal_school),
                                                new UmCallback<PersonField>() {
                                                    @Override
                                                    public void onSuccess(PersonField customField) {
                                                        if (customField != null) {
                                                            PersonCustomFieldValue personCustomFieldValue =
                                                                    new PersonCustomFieldValue();
                                                            personCustomFieldValue.setFieldValue(studentSchool);
                                                            personCustomFieldValue
                                                                    .setPersonCustomFieldValuePersonUid(personPersonUid);
                                                            personCustomFieldValue
                                                                    .setPersonCustomFieldValuePersonCustomFieldUid(
                                                                            customField.getPersonCustomFieldUid());

                                                            personCustomFieldValueDao.insert(personCustomFieldValue);

                                                        } else {
                                                            System.out.println("Unable to create Custom Value");
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(Throwable exception) {
                                                        exception.printStackTrace();
                                                    }
                                                });
                                    }

                                    if(personPersonUid != null && personGroupUid != null){
                                        //Done teachers person - create clazzMember

                                        if(role == ClazzMember.ROLE_TEACHER){

                                            //Create EntityRole
                                            EntityRole entityRole = new EntityRole();
                                            entityRole.setErTableId(Clazz.TABLE_ID);
                                            entityRole.setErEntityUid(thisClazz.getClazzUid());
                                            entityRole.setErRoleUid(teacherRoleUid);

                                            repository.getEntityRoleDao().insertAsync(entityRole,
                                                    new UmCallback<Long>() {
                                                        @Override
                                                        public void onSuccess(Long result) {
                                                            if(result != null){

                                                                //For a specific clazz
                                                                EntityRole newEntityClazzSpecific = new EntityRole();
                                                                newEntityClazzSpecific.setErGroupUid(personGroupUid);
                                                                newEntityClazzSpecific.setErRoleUid(teacherRoleUid);
                                                                newEntityClazzSpecific.setErTableId(Clazz.TABLE_ID);
                                                                newEntityClazzSpecific.setErEntityUid(thisClazz.getClazzUid());
                                                                repository.getEntityRoleDao()
                                                                        .insertAsync(newEntityClazzSpecific, new UmCallback<Long>() {
                                                                            @Override
                                                                            public void onSuccess(Long entityRoleDaoUid) {
                                                                                if(entityRoleDaoUid != null){
                                                                                    checkClazzMember(thisClazz, bulkLine,
                                                                                            personPersonUid, role);
                                                                                }else{
                                                                                    view.showMessage("Something went wrong in clazz entity roles");
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onFailure(Throwable exception) {
                                                                                exception.printStackTrace();
                                                                            }
                                                                        });

                                                            }else{
                                                                view.showMessage("Something went wrong");
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Throwable exception) {
                                                            exception.printStackTrace();
                                                        }
                                                    });
                                        }else{
                                            //Create ClazzMember
                                            checkClazzMember(thisClazz, bulkLine,
                                                    personPersonUid, role);
                                        }


                                    }else{
                                        System.out.println("ERROR: UNABLE TO PERSIST PERSON!");
                                    }
                                }

                                @Override
                                public void onFailure(Throwable exception) {
                                    exception.printStackTrace();
                                }
                            });

                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }


    public class BulkUploadLine{

        private static final String CSV_DELIMITTER = ",";
        private static final int INDEX_PERSON_ID=0;
        private static final int INDEX_FIRST_NAME=1;
        private static final int INDEX_FATHER_NAME=2;
        private static final int INDEX_FAMILY_NAME=3;
        private static final int INDEX_USERNAME=4;
        private static final int INDEX_PASSWORD=5;
        private static final int INDEX_LAST_NAME=6;
        private static final int INDEX_GENDER=7;
        private static final int INDEX_DOB=8;
        private static final int INDEX_MOTHER_NAME=9;
        private static final int INDEX_PHONE_NO=10;
        private static final int INDEX_ADDRESS=11;
        private static final int INDEX_CURRENT_SCHOOL=12;
        private static final int INDEX_LOCATION_NAME1=13;
        private static final int INDEX_LOCATION_NAME2=14;
        private static final int INDEX_LOCATION_NAME3=15;
        private static final int INDEX_LOCATION_LEAF_NAME=16;
        private static final int INDEX_LOCATION_ID=17;
        private static final int INDEX_CLASS_NAME=18;
        private static final int INDEX_CLASS_LOCATION=19;
        private static final int INDEX_TEACHER_NAME=20;
        private static final int INDEX_TEACHER_PHONE_NO=21;
        private static final int INDEX_TEACHER_FIRST_NAME=22;
        private static final int INDEX_TEACHER_LAST_NAME=23;
        private static final int INDEX_TEACHER_ID=24;
        private static final int INDEX_TEACHER_USERNAME=25;
        private static final int INDEX_TEACHER_PASSWORD=26;

        private String line;
        private String[] data;

        public String person_id, first_name, father_name, family_name, username, password,
                last_name, gender, dob, mother_name, phone_no, address, school, location1, location2,
                location3, locationLeaf, locationId, class_name, class_location, teacher_name,
                teacher_phone_no, teacher_first_name, teacher_last_name, teacher_id, teacher_username,
                teacher_password;

        BulkUploadLine(String line){
            this.line=line;
            this.data = line.split(CSV_DELIMITTER);
            person_id = data[INDEX_PERSON_ID];
            first_name = data[INDEX_FIRST_NAME];
            father_name = data[INDEX_FATHER_NAME];
            family_name = data[INDEX_FAMILY_NAME];
            username = data[INDEX_USERNAME];
            password = data[INDEX_PASSWORD];
            last_name = data[INDEX_LAST_NAME];
            gender = data[INDEX_GENDER];
            dob = data[INDEX_DOB];
            mother_name = data[INDEX_MOTHER_NAME];
            phone_no = data[INDEX_PHONE_NO];
            address = data[INDEX_ADDRESS];
            school = data[INDEX_CURRENT_SCHOOL];
            location1 = data[INDEX_LOCATION_NAME1];
            location2 = data[INDEX_LOCATION_NAME2];
            location3 = data[INDEX_LOCATION_NAME3];
            locationLeaf = data[INDEX_LOCATION_LEAF_NAME];
            locationId = data[INDEX_LOCATION_ID];
            class_name = data[INDEX_CLASS_NAME];
            class_location = data[INDEX_CLASS_LOCATION];
            teacher_name = data[INDEX_TEACHER_NAME];
            teacher_phone_no = data[INDEX_TEACHER_PHONE_NO];
            teacher_first_name = data[INDEX_TEACHER_FIRST_NAME];
            teacher_last_name = data[INDEX_TEACHER_LAST_NAME];
            teacher_id = data[INDEX_TEACHER_ID];
            teacher_username = data[INDEX_TEACHER_USERNAME];
            teacher_password = data[INDEX_TEACHER_PASSWORD];

        }








    }

}
