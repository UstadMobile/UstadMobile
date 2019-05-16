package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzMemberDao;
import com.ustadmobile.core.db.dao.CustomFieldDao;
import com.ustadmobile.core.db.dao.CustomFieldValueDao;
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
import com.ustadmobile.lib.db.entities.CustomField;
import com.ustadmobile.lib.db.entities.CustomFieldValue;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue;
import com.ustadmobile.lib.db.entities.PersonField;
import com.ustadmobile.lib.db.entities.PersonGroupMember;
import com.ustadmobile.lib.db.entities.Role;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private CustomFieldDao customFieldDao;
    private CustomFieldValueDao customFieldValueDao;
    private PersonGroupMemberDao personGroupMemberDao;
    private EntityRoleDao entityRoleDao;
    private PersonCustomFieldDao personFieldDao;
    private PersonCustomFieldValueDao personCustomFieldValueDao;

    private boolean thereWasAnError = false;

    BulkUploadLine bulkLine;

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
        customFieldDao = repository.getCustomFieldDao();
        customFieldValueDao = repository.getCustomFieldValueDao();
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

    public void startParsing(){
        //Start with header line at position 0:
        String headerLine = lines.get(0);
        bulkLine = new BulkUploadLine();
        bulkLine.processHeader(headerLine);

        //Continue with next line data.
        processNextLine();

    }

    /**
     * Start processing the next line
     */
    private void processNextLine(){
        currentPosition ++;
        System.out.println("BULK UPLOAD LINE : " + currentPosition);

        view.updateProgressValue(currentPosition, lines.size());

        if(currentPosition >= lines.size()){
            //If at the end of the line, finish the activity.
            if(view.getAllErrors().size()>0){
                //Don't finish activity yet. Remain to show errors.
                if(thereWasAnError){
                    view.setErrorHeading(MessageID.please_review_errors);
                }else{
                    view.setErrorHeading(MessageID.please_review_warnings);
                }

            }else {
                view.finish();
            }
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
        bulkLine.setLine(line);
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
                    view.addError("WARNING: More than one instances of " + locationGovernorateTitle +
                                    " location. Please delete duplicate Locations.",
                            false);
                    locationGovernorate.setLocationUid(location1s.get(0).getLocationUid());
                    move = true;
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
                        view.addError("WARNING: More than one instances of " + locationDistrictTitle +
                                        " location. Please delete duplicate Locations.",
                                false);
                        move2 = true;
                        //Location already exists. Getting uid.
                        locationDistrict.setLocationUid(location2s.get(0).getLocationUid());

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
                            view.addError("WARNING: More than one instances of " + locationTownTitle +
                                            " location. Please delete duplicate Locations.",
                                    false);
                            move3 = true;
                            locationTown.setLocationUid(location3s.get(0).getLocationUid());
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
                                view.addError("WARNING: More than one instances of " + locationLeafTitle +
                                                " location. Please delete duplicate Locations.",
                                        false);
                                locationLeaf.setLocationUid(locationLeafs.get(0).getLocationUid());
                                moveOn = true;
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
            if (clazzLocations.size() > 0) {
                if(clazzLocations.size() > 1){
                    //Maybe we alert user that multi locations ?
                }
                //Location exists and is unique
                Location thisClazzLocation = clazzLocations.get(0);
                thisClazz.setClazzLocationUid(thisClazzLocation.getLocationUid());
                thisClazz.setClazzActive(true);
                thisClazz.setClazzLocationUid(locationLeaf.getLocationUid());

                clazzDao.update(thisClazz);

                //Class Custom field
                Iterator<Map.Entry<Long, Integer>> classCustomIterator =
                        bulkLine.classCustomFieldToIndex.entrySet().iterator();
                while(classCustomIterator.hasNext()){
                    Map.Entry<Long, Integer> customFieldAndColMap = classCustomIterator.next();
                    Long customFieldUid = customFieldAndColMap.getKey();
                    Integer colIndex = customFieldAndColMap.getValue();
                    String value = bulkLine.getData()[colIndex];
                    if(value!= null && !value.isEmpty()){
                        persistCustomFieldSync(customFieldUid, thisClazz.getClazzUid(), value);
                    }
                }


                //Move on
                checkPerson(thisClazz, bulkLine, ClazzMember.ROLE_TEACHER);

            }else{
                //Location does not exist.
                view.addError("ERROR: LOCATION : " + clazzLocation
                        + " DOESN'T EXIST", true);
                thereWasAnError = true;
            }

        }else if (clazzes.size() > 1){   //Multiple clazzes with that name (ERROR)
            view.addError("ERROR : MULTIPLE CLAZZ WITH NAME: " + clazzName, true);
            thereWasAnError = true;
        }else{
            thisClazz = clazzes.get(0);
            //Not updating clazz. Moving on
            checkPerson(thisClazz, bulkLine, ClazzMember.ROLE_TEACHER);
        }

    }

    private void persistCustomFieldSync(long fieldUid, long entityUid, String value){
        CustomFieldValue customValue =
                customFieldValueDao.findValueByCustomFieldUidAndEntityUidSync(
                        fieldUid, entityUid);

        if(customValue == null) {
            customValue = new CustomFieldValue();
            customValue.setCustomFieldValueFieldUid(fieldUid);
            customValue.setCustomFieldValueEntityUid(entityUid);
            customValue.setCustomFieldValueValue(value);
            customFieldValueDao.insert(customValue);
        }else{
            customValue.setCustomFieldValueValue(value);
            customFieldValueDao.update(customValue);
        }
    }

    private void checkPerson(Clazz thisClazz, BulkUploadLine bulkLine, int role){
        String personUid = bulkLine.person_id;
        String teacherUsername = bulkLine.teacher_username;
        String teacherId = bulkLine.teacher_id;
        String teacherFirstName = bulkLine.teacher_first_name;
        String teacherLastName = bulkLine.teacher_last_name;
        String teacherPhoneNo = bulkLine.teacher_phone_no;

        String stuentFirstName = bulkLine.first_name;
        String studentLastName = bulkLine.last_name;
        String studentGender = bulkLine.gender;
        String studentMotherName = bulkLine.mother_name;
        String studentMotherNum = bulkLine.mother_num;
        String studentPhoneNo = bulkLine.phone_no;
        String studentSchool = bulkLine.school;
        String studentAddress = bulkLine.address;
        String studentFatherName = bulkLine.father_name;
        String studentFatherNum = bulkLine.father_num;
        String studentDateOfBirth = bulkLine.dob;

        String username;


        if(role == ClazzMember.ROLE_TEACHER){
            if(teacherUsername.isEmpty() && !teacherId.isEmpty()){
                teacherUsername = teacherId;
            }
            username = teacherUsername;
        }else{
            username = personUid;
        }

        Person thePerson = personDao.findByUsername(username);

        if(thePerson != null){
            //person object exists - Not creating extra fields (inc custom fields)

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
                if(!teacherPhoneNo.isEmpty())
                    person.setPhoneNum(teacherPhoneNo);
                person.setUsername(username);

            }else{
                person.setUsername(username);
                person.setFirstNames(stuentFirstName);
                person.setLastName(studentLastName);
                if(!studentMotherName.isEmpty())
                    person.setMotherName(studentMotherName);
                if(!studentMotherNum.isEmpty()){
                    person.setMotherNum(studentMotherNum);
                }
                if(!studentFatherName.isEmpty())
                    person.setFatherName(studentFatherName);
                if(!studentFatherNum.isEmpty())
                    person.setFatherNumber(studentFatherNum);
                if(!studentAddress.isEmpty())
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

                    //Creating custom fields (old way):
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
                            view.addError("Unable to create Custom Value", true);
                            thereWasAnError = true;
                        }

                    }

                    //Creating custom fields (new way)
                    if(role == ClazzMember.ROLE_STUDENT){

                        //Student Custom field
                        Iterator<Map.Entry<Long, Integer>> studentCustomIterator =
                                bulkLine.studentCustomFieldToIndex.entrySet().iterator();
                        while(studentCustomIterator.hasNext()){
                            Map.Entry<Long, Integer> customFieldAndColMap = studentCustomIterator.next();
                            Long customFieldUid = customFieldAndColMap.getKey();
                            Integer colIndex = customFieldAndColMap.getValue();
                            String value = bulkLine.getData()[colIndex];
                            if(value!= null && !value.isEmpty()){
                                persistCustomFieldSync(customFieldUid, person.getPersonUid(), value);
                            }
                        }

                    }else if(role == ClazzMember.ROLE_TEACHER){
                        //Teacher Custom field
                        Iterator<Map.Entry<Long, Integer>> teacherCustomIterator =
                                bulkLine.teacherCustomFieldToIndex.entrySet().iterator();
                        while(teacherCustomIterator.hasNext()){
                            Map.Entry<Long, Integer> customFieldAndColMap = teacherCustomIterator.next();
                            Long customFieldUid = customFieldAndColMap.getKey();
                            Integer colIndex = customFieldAndColMap.getValue();
                            String value = bulkLine.getData()[colIndex];
                            if(value!= null && !value.isEmpty()){
                                persistCustomFieldSync(customFieldUid, person.getPersonUid(), value);
                            }
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
                                    view.addError("Something went wrong in clazz entity roles", true);
                                    thereWasAnError = true;
                                }

                            }else{
                                view.addError("Unable to persist EntityRole ", true);
                                thereWasAnError = true;
                            }

                        }else{
                            //Create ClazzMember
                            checkClazzMember(thisClazz, bulkLine,
                                    personPersonUid, role);
                        }

                    }else{
                        view.addError("ERROR: UNABLE TO PERSIST PERSON!", true);
                        thereWasAnError = true;
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
                view.addError("ERROR: UNABLE TO PERSIST CLAZZMEMBER??", true);
                thereWasAnError = true;
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
                            view.addError("Something went wrong in clazz entity roles", true);
                            thereWasAnError = true;
                        }
                    }else{
                        view.addError("Unable to persist entity role", true);
                        thereWasAnError = true;
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

        private String line;
        private String[] data;
        private String[] header;
        private String headerLine;
        private String[] customClassData;
        private String[] customStudentData;
        private String[] customTeacherData;

        public HashMap<Long, Integer> studentCustomFieldToIndex;
        public HashMap<Long, Integer> teacherCustomFieldToIndex;
        public HashMap<Long, Integer> classCustomFieldToIndex;

        public String[] getData() {
            return data;
        }

        UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);
        CustomFieldDao customFieldDao = repository.getCustomFieldDao();


        String person_id = "", first_name = "", father_name = "", family_name = "", username = "",
                password = "", last_name = "", gender = "", dob = "", mother_name = "",
                phone_no = "", address = "", school = "", location1 = "", location2 = "",
                location3 = "", locationLeaf = "", class_name = "", class_location = "",
                teacher_name = "", teacher_phone_no = "", teacher_first_name = "",
                teacher_last_name = "", teacher_id = "", teacher_username = "",
                teacher_password = "", mother_num = "", father_num = "", location_id;

        private String CSV_DELIMITTER = ",";

        private int INDEX_PERSON_ID=-1;
        private int INDEX_FIRST_NAME=-1;
        private int INDEX_FATHER_NAME=-1;
        private int INDEX_FAMILY_NAME=-1;
        private int INDEX_USERNAME=-1;
        private int INDEX_PASSWORD=-1;
        private int INDEX_LAST_NAME=-1;
        private int INDEX_GENDER=-1;
        private int INDEX_DOB=-1;
        private int INDEX_MOTHER_NAME=-1;
        private int INDEX_MOTHER_NUMBER=-1;
        private int INDEX_FATHER_NUMBER=-1;
        private int INDEX_PHONE_NO=-1;
        private int INDEX_ADDRESS=-1;
        private int INDEX_CURRENT_SCHOOL=-1;
        private int INDEX_LOCATION_NAME1=-1;
        private int INDEX_LOCATION_NAME2=-1;
        private int INDEX_LOCATION_NAME3=-1;
        private int INDEX_LOCATION_LEAF_NAME=-1;
        private int INDEX_CLASS_NAME=-1;
        private int INDEX_CLASS_LOCATION=-1;
        private int INDEX_TEACHER_NAME=-1;
        private int INDEX_TEACHER_PHONE_NO=-1;
        private int INDEX_TEACHER_FIRST_NAME=-1;
        private int INDEX_TEACHER_LAST_NAME=-1;
        private int INDEX_TEACHER_ID=-1;
        private int INDEX_TEACHER_USERNAME=-1;
        private int INDEX_TEACHER_PASSWORD=-1;
        private int INDEX_LOCATION_ID = -1;

        public static final int CUSTOM_FIELD_STUDENT = 1;
        public static final int CUSTOM_FIELD_TEACHER = 2;
        public static final int CUSTOM_FIELD_CLASS = 3;

        BulkUploadLine(){
            studentCustomFieldToIndex = new HashMap<>();
            teacherCustomFieldToIndex = new HashMap<>();
            classCustomFieldToIndex = new HashMap<>();
        }

        void reset(){
            person_id  = ""; first_name  = ""; father_name  = ""; family_name  = ""; username  = "";
            password  = ""; last_name  = ""; gender  = ""; dob  = ""; mother_name  = "";
            phone_no  = ""; address  = ""; school  = ""; location1  = ""; location2  = "";
            location3  = ""; locationLeaf  = ""; class_name  = ""; class_location  = "";
            teacher_name  = ""; teacher_phone_no  = ""; teacher_first_name  = "";
            teacher_last_name  = ""; teacher_id  = ""; teacher_username  = "";
            teacher_password  = ""; mother_num  = ""; father_num  = ""; location_id = "";

        }
        void setLine(String line){
            this.line=line;
            reset();
            this.data = line.split(CSV_DELIMITTER);
            //There might be a better way, but i cbb
            if(INDEX_PERSON_ID>-1)
            person_id = data[INDEX_PERSON_ID];
            if(INDEX_FIRST_NAME>-1)
            first_name = data[INDEX_FIRST_NAME];
            if(INDEX_FATHER_NAME>-1)
            father_name = data[INDEX_FATHER_NAME];
            if(INDEX_FAMILY_NAME>-1)
            family_name = data[INDEX_FAMILY_NAME];
            if(INDEX_USERNAME>-1)
            username = data[INDEX_USERNAME];
            if(INDEX_PASSWORD>-1)
            password = data[INDEX_PASSWORD];
            if(INDEX_LAST_NAME>-1)
            last_name = data[INDEX_LAST_NAME];
            if(INDEX_GENDER>-1)
            gender = data[INDEX_GENDER];
            if(INDEX_DOB>-1)
            dob = data[INDEX_DOB];
            if(INDEX_MOTHER_NAME>-1)
            mother_name = data[INDEX_MOTHER_NAME];
            if(INDEX_PHONE_NO>-1)
            phone_no = data[INDEX_PHONE_NO];
            if(INDEX_ADDRESS>-1)
            address = data[INDEX_ADDRESS];
            if(INDEX_CURRENT_SCHOOL>-1)
            school = data[INDEX_CURRENT_SCHOOL];
            if(INDEX_LOCATION_NAME1>-1)
            location1 = data[INDEX_LOCATION_NAME1];
            if(INDEX_LOCATION_NAME2>-1)
            location2 = data[INDEX_LOCATION_NAME2];
            if(INDEX_LOCATION_NAME3>-1)
            location3 = data[INDEX_LOCATION_NAME3];
            if(INDEX_LOCATION_LEAF_NAME>-1)
            locationLeaf = data[INDEX_LOCATION_LEAF_NAME];
            if(INDEX_CLASS_NAME>-1)
            class_name = data[INDEX_CLASS_NAME];
            if(INDEX_CLASS_LOCATION>-1)
            class_location = data[INDEX_CLASS_LOCATION];
            if(INDEX_TEACHER_NAME>-1)
            teacher_name = data[INDEX_TEACHER_NAME];
            if(INDEX_TEACHER_PHONE_NO>-1)
            teacher_phone_no = data[INDEX_TEACHER_PHONE_NO];
            if(INDEX_TEACHER_FIRST_NAME>-1)
            teacher_first_name = data[INDEX_TEACHER_FIRST_NAME];
            if(INDEX_TEACHER_LAST_NAME>-1)
            teacher_last_name = data[INDEX_TEACHER_LAST_NAME];
            if(INDEX_TEACHER_ID>-1)
            teacher_id = data[INDEX_TEACHER_ID];
            if(INDEX_TEACHER_USERNAME>-1)
            teacher_username = data[INDEX_TEACHER_USERNAME];
            if(INDEX_TEACHER_PASSWORD>-1)
            teacher_password = data[INDEX_TEACHER_PASSWORD];
            if(INDEX_MOTHER_NUMBER>-1)
            mother_num = data[INDEX_MOTHER_NUMBER];
            if(INDEX_FATHER_NUMBER>-1)
            father_num = data[INDEX_FATHER_NUMBER];
            if(INDEX_LOCATION_ID>-1)
            location_id = data[INDEX_LOCATION_ID];

        }

        String getCamelCaseFromTypeCase(String typeCase){

            String titleCase = "";
            typeCase = typeCase.toLowerCase();
            String[] allWords = typeCase.split(" ");
            boolean first = true;
            for(int i=0; i< allWords.length;i++){
                String word = allWords[i];
                if(!word.isEmpty()){
                    String camelWord = word;
                    if(first){
                        first=false;
                    }else{
                        camelWord = word.substring(0, 1).toUpperCase() + word.substring(1);
                    }

                    titleCase = titleCase + camelWord;
                }
            }
            return titleCase;
        }

        void processHeader(String headerLine){
            this.headerLine = headerLine;
            int colIndex = 0;
            this.header = headerLine.split(CSV_DELIMITTER);

            for(int i=0;i<header.length;i++){

                String everyHeader = header[i];
                everyHeader = everyHeader.trim();
                everyHeader = everyHeader.replaceAll("\\P{Print}", "");
                everyHeader = everyHeader.replace("\uFEFF", "");
                everyHeader = everyHeader.toLowerCase();
                if(everyHeader.startsWith("student ")){
                    String fieldTC = getCamelCaseFromTypeCase(everyHeader.substring("student ".length()));
                    switch (fieldTC){
                        case "personId":
                            INDEX_PERSON_ID = colIndex;
                            break;
                        case "firstNames":
                            INDEX_FIRST_NAME = colIndex;
                            break;
                        case "lastName":
                            INDEX_LAST_NAME = colIndex;
                            break;
                        case "gender":
                            INDEX_GENDER = colIndex;
                            break;
                        case "dob":
                            INDEX_DOB = colIndex;
                            break;
                        case "motherName":
                            INDEX_MOTHER_NAME = colIndex;
                            break;
                        case "motherNum":
                            INDEX_MOTHER_NUMBER = colIndex;
                            break;
                        case "fatherName":
                            INDEX_FATHER_NAME = colIndex;
                            break;
                        case "fatherNumber":
                             INDEX_FATHER_NUMBER = colIndex;
                            break;
                        case "address":
                            INDEX_ADDRESS = colIndex;
                            break;
                        default:
                            //Lookup custom field
                            findCustomField(fieldTC, Person.TABLE_ID, colIndex, CUSTOM_FIELD_STUDENT);
                            break;
                    }

                }else if(everyHeader.startsWith("teacher ")){
                    String fieldTC = getCamelCaseFromTypeCase(everyHeader.substring("teacher ".length()));
                    switch (fieldTC){
                        case "id":
                            INDEX_TEACHER_ID = colIndex;
                            break;
                        case "firstNames":
                            INDEX_TEACHER_FIRST_NAME = colIndex;
                            break;
                        case "lastName":
                            INDEX_TEACHER_LAST_NAME = colIndex;
                            break;
                        case "phoneNum":
                            INDEX_TEACHER_PHONE_NO = colIndex;
                            break;
                        case "username":
                            INDEX_TEACHER_USERNAME = colIndex;
                            break;
                        default:
                            findCustomField(fieldTC, Person.TABLE_ID, colIndex, CUSTOM_FIELD_TEACHER);
                            break;

                    }
                }else if(everyHeader.startsWith("class ")){
                    String fieldTC = getCamelCaseFromTypeCase(everyHeader.substring("class ".length()));
                    switch (fieldTC){
                        case "name":
                            INDEX_CLASS_NAME = colIndex;
                            break;
                        case "location":
                            INDEX_CLASS_LOCATION = colIndex;
                            break;
                        default:
                            //Find Custom
                            findCustomField(fieldTC, Clazz.TABLE_ID, colIndex, CUSTOM_FIELD_CLASS);
                            break;

                    }

                }else if(everyHeader.startsWith("location ")){
                    String fieldTC = getCamelCaseFromTypeCase(everyHeader.substring("location ".length()));
                    switch (fieldTC){
                        case "id":
                            INDEX_LOCATION_ID = colIndex;
                            break;
                        case "governorate":
                            INDEX_LOCATION_NAME1 = colIndex;
                            break;
                        case "district":
                            INDEX_LOCATION_NAME2 = colIndex;
                            break;
                        case "town":
                            INDEX_LOCATION_NAME3 = colIndex;
                            break;
                        case "name":
                            INDEX_LOCATION_LEAF_NAME = colIndex;
                            break;
                        default:
                            //NO CUSTOM FIELD FOR LOCATION.
                            view.addError("Can't find location value: " + fieldTC, true);
                            thereWasAnError = true;
                            break;

                    }

                }//else nothing to process. Error log it ?
                else{
                    view.addError("Error cannot figure what this is: " + everyHeader, true);
                    thereWasAnError = true;
                }

                colIndex++;
            }

        }

        void findCustomField(String fieldName, int entity, int colIndex, int type){
            customFieldDao.findByFieldNameAndEntityTypeAsync(fieldName, entity,
                    new UmCallback<List<CustomField>>() {
                @Override
                public void onSuccess(List<CustomField> result) {
                    CustomField cf;
                    if(result!=null && result.size()>0){
                        cf = result.get(0);
                        switch (type){
                            case CUSTOM_FIELD_STUDENT:
                                studentCustomFieldToIndex.put(cf.getCustomFieldUid(), colIndex);
                                break;
                            case CUSTOM_FIELD_TEACHER:
                                teacherCustomFieldToIndex.put(cf.getCustomFieldUid(), colIndex);
                                break;
                            case CUSTOM_FIELD_CLASS:
                                classCustomFieldToIndex.put(cf.getCustomFieldUid(), colIndex);
                                break;
                        }
                    }
                }

                @Override
                public void onFailure(Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }



    }

}
