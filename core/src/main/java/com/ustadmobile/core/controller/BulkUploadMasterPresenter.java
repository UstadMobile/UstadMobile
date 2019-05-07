package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.EntityRoleDao;
import com.ustadmobile.core.db.dao.LocationDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldDao;
import com.ustadmobile.core.db.dao.PersonCustomFieldValueDao;
import com.ustadmobile.core.db.dao.PersonDao;
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
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonField;
import com.ustadmobile.lib.db.entities.PersonGroupMember;
import com.ustadmobile.lib.db.entities.Role;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class BulkUploadMasterPresenter extends UstadBaseController<BulkUploadMasterView> {

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    private int currentPosition = 0;
    private List<String> lines;
    private long teacherRoleUid = 0L;
    private String chosenTZ;
    private List<String> allTZ;

    private LocationDao locationDao;
    private RoleDao roleDao;
    private ClazzDao clazzDao;
    private ClazzMemberDao clazzMemberDao;
    private PersonDao personDao;
    private PersonGroupMemberDao personGroupMemberDao;
    private EntityRoleDao entityRoleDao;
    private PersonCustomFieldDao personFieldDao;
    private PersonCustomFieldValueDao personCustomFieldValueDao;

    public BulkUploadMasterPresenter(Object context, Hashtable arguments,
                                     BulkUploadMasterView view) {
        super(context, arguments, view);
        locationDao = repository.getLocationDao();
        roleDao = repository.getRoleDao();
        clazzDao = repository.getClazzDao();
        clazzMemberDao = repository.getClazzMemberDao();
        personDao = repository.getPersonDao();
        personGroupMemberDao = repository.getPersonGroupMemberDao();
        entityRoleDao = repository.getEntityRoleDao();
        personFieldDao = repository.getPersonCustomFieldDao();
        personCustomFieldValueDao =repository.getPersonCustomFieldValueDao();
    }

    @Override
    public void onCreate(Hashtable savedState) {
        super.onCreate(savedState);

        //Set all timezones to view
        allTZ = Arrays.asList(TimeZone.getAvailableIDs());
        view.setTimeZonesList(allTZ);
        //Get default timezone
        chosenTZ = TimeZone.getDefault().getID();
        //Get Teacher role
        getRole();

    }

    /**
     * Update chosen timezone
     */
    public void setTimeZoneSelected(int position, long id){
        chosenTZ = allTZ.get(position);
    }

    /**
     * Start processing the next line
     */
    public void processNextLine(){
        currentPosition ++;
        System.out.println("BULK UPLOAD LINE : " + currentPosition);

        view.updateProgressValue(currentPosition, lines.size());

        if(currentPosition >= lines.size()){
            //If at the end of the line, finish the activity.
            view.finish();
        }else {
            //Get the line and Start processing it.
            parseLine(lines.get(currentPosition));
        }

    }

    /**
     * Every line to be parsed contains all the information including Student, Tecaher, Class,
     * Location, etc. This is why we need to run things in order.
     * We need to set up in a way where we first make sure we get all Roles and Daos required.
     * Then we need to process Location. Then we need proces Class, then Teachers and then students.
     *
     * @param line  The bulk upload line
     */
    private void parseLine(String line){
        System.out.println("Processing line of length: " + line.length());

        BulkUploadLine bulkLine = new BulkUploadLine(line);

        //1. Location
        //2. Clazz
        //3. Teacher
        //4. Student

        processLocations(bulkLine);

    }


    /**
     * Process the 4 levels of locations in the bulk upload line.
     * @param bulkLine  The line
     */
    private void processLocations(BulkUploadLine bulkLine){

        String locationGovernorateTitle = bulkLine.location1;
        String locationDistrictTitle = bulkLine.location2;
        String locationTownTitle = bulkLine.location3;
        String locationLeafTitle = bulkLine.locationLeaf;

        Location locationGovernorate = new Location();
        locationGovernorate.setTitle(locationGovernorateTitle);
        locationGovernorate.setTimeZone(chosenTZ);

        Location locationDistrict = new Location();
        locationDistrict.setTitle(locationDistrictTitle);
        locationDistrict.setTimeZone(chosenTZ);

        Location locationTown = new Location();
        locationTown.setTitle(locationTownTitle);
        locationTown.setTimeZone(chosenTZ);

        Location locationLeaf = new Location();
        locationLeaf.setTitle(locationLeafTitle);
        locationLeaf.setTimeZone(chosenTZ);

        locationDao.findByTitleAsync(locationGovernorateTitle, new UmCallback<List<Location>>() {
            @Override
            public void onSuccess(List<Location> location1s) {

                boolean move = false;
                if(location1s.size() == 0){
                    //Location not created. Create it.
                    locationGovernorate.setLocationUid(locationDao.insert(locationGovernorate));
                    move = true;
                }else if(location1s.size() == 1){
                    //Location already exists. Getting uid.
                    locationGovernorate.setLocationUid(location1s.get(0).getLocationUid());
                    move = true;
                }else{
                    System.out.println("ERROR: Location: 1st level: More than 1 with the same title");
                }

                if(move) {
                    List<Location> location2s = locationDao.findByTitle(locationDistrictTitle);
                    boolean move2 = false;
                    if(location2s.size() ==0){
                        move2 = true;
                        //Location not created. Create it.
                        locationDistrict.setParentLocationUid(locationGovernorate.getLocationUid());
                        locationDistrict.setLocationUid(locationDao.insert(locationDistrict));
                    }else if(location2s.size() == 1){
                        move2 = true;
                        //Location already exists. Getting uid.
                        locationDistrict.setLocationUid(location2s.get(0).getLocationUid());
                    }else{
                        System.out.println("ERROR: Location: 2nd level: More than 1 with the same title");
                    }

                    if(move2){
                        List<Location> location3s = locationDao.findByTitle(locationTownTitle);
                        boolean move3 = false;
                        if(location3s.size() == 0){
                            move3 = true;
                            locationTown.setParentLocationUid(locationDistrict.getLocationUid());
                            locationTown.setLocationUid(locationDao.insert(locationTown));
                        }else if(location3s.size() == 1){
                            move3 = true;
                            locationTown.setLocationUid(location3s.get(0).getLocationUid());
                        }else{
                            System.out.println("ERROR: Location: 3rd level: More than 1" +
                                    " with the same title");
                        }

                        if(move3){
                            //Move on the the leaf level
                            List<Location> locationLeafs = locationDao.findByTitle(locationLeafTitle);

                            boolean moveOn = false;
                            if(locationLeafs.size() == 0){
                                //Location leaf not created. Setting parent and
                                //persisting to get uid.
                                locationLeaf.setParentLocationUid(locationTown.getLocationUid());
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
                            if(moveOn) {
                                processClazz(bulkLine, locationLeaf);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    private void processClazz(BulkUploadLine bulkLine, Location locationLeaf){
        //2. Class
        String clazzName = bulkLine.class_name;
        String clazzLocation = bulkLine.class_location;

        //Get clazz by name:
        List<Clazz> clazzes = clazzDao.findByClazzName(clazzName);
        Clazz thisClazz;
        if(clazzes.size() == 0){   //No clazzes with that name.
            //Create clazz
            thisClazz = new Clazz();
            thisClazz.setClazzName(clazzName);

            thisClazz.setClazzUid(clazzDao.insert(thisClazz));

            //Add location
            List<Location> clazzLocations = locationDao.findByTitle(clazzLocation);
            if (clazzLocations.size() == 1) {
                //Location exists and is unique
                Location thisClazzLocation = clazzLocations.get(0);
                thisClazz.setClazzLocationUid(thisClazzLocation.getLocationUid());
                thisClazz.setClazzActive(true);
                thisClazz.setClazzLocationUid(locationLeaf.getLocationUid());

                clazzDao.update(thisClazz);
                //Move on
                checkPerson(thisClazz, bulkLine, ClazzMember.ROLE_TEACHER);

            }else{
                //Location does not exist.
                System.out.println("FAIL: LOCATION : " + clazzLocation
                        + " DOESN'T EXIST");
            }

        }else if (clazzes.size() > 1){   //Multiple clazzes with that name (ERROR)
            System.out.println("ERROR : MULTIPLE CLAZZ WITH NAME: " + clazzName);
        }else{
            thisClazz = clazzes.get(0);
            //Not updating clazz. Moving on
            checkPerson(thisClazz, bulkLine, ClazzMember.ROLE_TEACHER);
        }

    }

    private void checkPerson(Clazz thisClazz, BulkUploadLine bulkLine, int role){
        String personUid = bulkLine.person_id;
        String teacherUsername = bulkLine.teacher_username;
        String teacherFirstName = bulkLine.teacher_first_name;
        String teacherLastName = bulkLine.teacher_last_name;
        String teacherPhoneNo = bulkLine.teacher_phone_no;

        String stuentFirstName = bulkLine.first_name;
        String studentLastName = bulkLine.last_name;
        String studentGender = bulkLine.gender;
        String studentMotherName = bulkLine.mother_name;
        String studentPhoneNo = bulkLine.phone_no;
        String studentSchool = bulkLine.school;
        String studentAddress = bulkLine.address;
        String studentFatherName = bulkLine.father_name;
        String studentDateOfBirth = bulkLine.dob;


        String username;
        if(role == ClazzMember.ROLE_TEACHER){
            username = teacherUsername;
        }else{
            username = personUid;
        }

        Person thePerson = personDao.findByUsername(username);

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
                person.setUsername(username);

            }else{
                person.setUsername(username);
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
            }

            person.setActive(true);

            personDao.createPersonWithGroupAsync(person,
             new UmCallback<PersonDao.PersonWithGroup>() {
                @Override
                public void onSuccess(PersonDao.PersonWithGroup personWithGroup) {
                    Long personPersonUid = personWithGroup.getPersonUid();
                    Long personGroupUid = personWithGroup.getPersonGroupUid();

                    if(role == ClazzMember.ROLE_STUDENT) {
                        PersonField customField = personFieldDao.findByLabelMessageIdSync(
                                String.valueOf(MessageID.current_formal_school));

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

                    if(personPersonUid != null && personGroupUid != null){
                        //Done teachers person - create clazzMember

                        if(role == ClazzMember.ROLE_TEACHER){

                            //Create EntityRole
                            EntityRole entityRole = new EntityRole();
                            entityRole.setErTableId(Clazz.TABLE_ID);
                            entityRole.setErEntityUid(thisClazz.getClazzUid());
                            entityRole.setErRoleUid(teacherRoleUid);

                            Long entityRoleUid = entityRoleDao.insert(entityRole);

                            if(entityRoleUid != null){

                                //For a specific clazz
                                EntityRole newEntityClazzSpecific = new EntityRole();
                                newEntityClazzSpecific.setErGroupUid(personGroupUid);
                                newEntityClazzSpecific.setErRoleUid(teacherRoleUid);
                                newEntityClazzSpecific.setErTableId(Clazz.TABLE_ID);
                                newEntityClazzSpecific.setErEntityUid(thisClazz.getClazzUid());

                                Long entityRoleDaoUid = entityRoleDao.insert(newEntityClazzSpecific);

                                if(entityRoleDaoUid != null){
                                    checkClazzMember(thisClazz, bulkLine,
                                            personPersonUid, role);
                                }else{
                                    view.showMessage("Something went wrong in clazz entity roles");
                                }

                            }else{
                                view.showMessage("Something went wrong");
                            }

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

    private void checkClazzMember(Clazz thisClazz, BulkUploadLine bulkLine, long personPersonUid,
                                  int role){

        ClazzMember clazzMember = clazzMemberDao.findByPersonUidAndClazzUid(personPersonUid,
                thisClazz.getClazzUid());

        if(clazzMember == null){
            //Create one.
            ClazzMember personClazzMember;
            personClazzMember = new ClazzMember();
            personClazzMember.setClazzMemberPersonUid(personPersonUid);
            personClazzMember.setClazzMemberClazzUid(thisClazz.getClazzUid());
            personClazzMember.setClazzMemberActive(true);
            personClazzMember.setDateJoined(System.currentTimeMillis());
            personClazzMember.setRole(role);

            Long clazzMemberUid = clazzMemberDao.insert(personClazzMember);

            if(clazzMemberUid != null) {
                if (role == ClazzMember.ROLE_TEACHER) {
                    checkPerson(thisClazz, bulkLine, ClazzMember.ROLE_STUDENT);
                }else{
                    processNextLine();

                }
            }else{
                System.out.println("ERROR: UNABLE TO PERSIST CLAZZMEMBER??");
            }


        }else{
            //Exists already
            if (role == ClazzMember.ROLE_TEACHER) {
                checkPerson(thisClazz, bulkLine, ClazzMember.ROLE_STUDENT);
            }else{
                processNextLine();

            }
        }
    }

    private void createEntityRoleForTeacherAndClazz(int role, Clazz thisClazz, long personPersonUid,
                                                    BulkUploadLine bulkLine){
        if(role == ClazzMember.ROLE_TEACHER){

            List<PersonGroupMember> allGroups =
                    personGroupMemberDao.findAllGroupWherePersonIsInSync(personPersonUid);

            long personGroupUid;
            if(allGroups != null && allGroups.size() > 0){
                //Get parent group :  ASSUMING ITS THE FIRST ONE
                personGroupUid = allGroups.get(0).getGroupMemberGroupUid();

                //Create EntityRole
                EntityRole entityRole = new EntityRole();
                entityRole.setErTableId(Clazz.TABLE_ID);
                entityRole.setErEntityUid(thisClazz.getClazzUid());
                entityRole.setErRoleUid(teacherRoleUid);

                List<EntityRole> entityRoles = entityRoleDao.findByEntitiyAndPersonGroupSync(
                        Clazz.TABLE_ID, thisClazz.getClazzUid(), personGroupUid);


                if(entityRoles.isEmpty()){
                    //Good, create one.

                    Long entityRoleUid = entityRoleDao.insert(entityRole);

                    if(entityRoleUid != null){

                        //For a specific clazz
                        EntityRole newEntityClazzSpecific = new EntityRole();
                        newEntityClazzSpecific.setErGroupUid(personGroupUid);
                        newEntityClazzSpecific.setErRoleUid(teacherRoleUid);
                        newEntityClazzSpecific.setErTableId(Clazz.TABLE_ID);
                        newEntityClazzSpecific.setErEntityUid(thisClazz.getClazzUid());
                        Long entityRoleDaoUid = entityRoleDao.insert(newEntityClazzSpecific);

                        if(entityRoleDaoUid != null){
                            checkClazzMember(thisClazz, bulkLine,
                                    personPersonUid, role);
                        }else{
                            view.showMessage("Something went wrong in clazz entity roles");
                        }


                    }else{
                        view.showMessage("Something went wrong");
                    }


                }else{
                    //Already created. continue.
                    checkClazzMember(thisClazz, bulkLine, personPersonUid, role);
                }

            }


        }else{
            checkClazzMember(thisClazz, bulkLine, personPersonUid, role);
        }
    }

    private void getRole(){

        roleDao.findByName(Role.ROLE_NAME_TEACHER, new UmCallback<Role>() {
            @Override
            public void onSuccess(Role teacherRole) {
                if(teacherRole != null){
                    teacherRoleUid = teacherRole.getRoleUid();
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

    private long getDOBFromString(String dateString){
        Locale currentLocale = Locale.getDefault();
        return UMCalendarUtil.getLongDateFromStringAndFormat(
                dateString,"dd/MM/yyyy", currentLocale);
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

    /**
     * Bulk upload line class
     */
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
