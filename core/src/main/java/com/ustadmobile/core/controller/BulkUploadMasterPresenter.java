package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.BulkUploadMasterView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

public class BulkUploadMasterPresenter extends UstadBaseController<BulkUploadMasterView> {

    public BulkUploadMasterPresenter(Object context, Hashtable arguments,
                                     BulkUploadMasterView view) {
        super(context, arguments, view);
    }

    UmAppDatabase repository = UmAccountManager.getRepositoryForActiveAccount(context);

    public void readFile(String filePath){
        File sourceFile = new File(filePath);
        readFile(sourceFile);
    }

    public void readFile(File sourceFile){
        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                parseMasterListLineToDatabase(line);
            }
            //Done processing:
            view.finish();
        } catch (FileNotFoundException e) {
            view.showMessage("File not found");
            e.printStackTrace();
        } catch (IOException e) {
            view.showMessage("Unable to process the file");
            e.printStackTrace();
        }
    }

    public void parseMasterListLineToDatabase(String line){
        System.out.println("Processing line of length: " + line.length());

        BulkUploadLine bulkLine = new BulkUploadLine(line);

        //1. Location
        //2. Clazz
        //3. Teacher
        //4. Student


        Location location1 = new Location();
        location1.setTitle(bulkLine.location1);

        Location location2 = new Location();
        location2.setTitle(bulkLine.location2);

        Location location3 = new Location();
        location3.setTitle(bulkLine.location3);

        Location locationLeaf = new Location();
        locationLeaf.setTitle(bulkLine.locationLeaf);


        //2. Class
        String clazzName = bulkLine.class_name;
        String clazzLocation = bulkLine.class_location;

        //Get clazz by name:
        repository.getClazzDao().findByClazzNameAsync(bulkLine.class_name,
                new UmCallback<List<Clazz>>() {
            @Override
            public void onSuccess(List<Clazz> clazzes) {
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
                                        if (result.size() == 1) {
                                            Location clazzLocation = result.get(0);
                                            thisClazz.setLocationUid(clazzLocation.getLocationUid());
                                            thisClazz.setClazzActive(true);

                                            repository.getClazzDao().insertAsync(thisClazz,
                                                    null);
                                            int x= 2+2;

                                        }else{
                                            System.out.println("FAIL: LOCATION : " + clazzLocation
                                                    + " DOESN'T EXIST");
                                        }

                                        //Move on to teachers :
                                        checkPerson(thisClazz, bulkLine, ClazzMember.ROLE_TEACHER);
                                            //Which will inturn move on to Students.
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
                    //Not updating clazz.
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });


    }

    public long getDOBFromString(String dateString){
        //TODO: This
        return 0;
    }

    public void checkStudents(Clazz thisClazz, BulkUploadLine bulkLine){

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


        repository.getPersonDao().findByUsernameAsync(teacherUsername, new UmCallback<Person>() {
            @Override
            public void onSuccess(Person thePerson) {
                if(thePerson != null){
                    //teacher exists
                }else{
                    //Create new teacher
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
                        if (studentGender.toLowerCase().startsWith("f")) {
                            person.setGender(Person.GENDER_FEMALE);
                        }else if(studentGender.toLowerCase().startsWith("m")){
                            person.setGender(Person.GENDER_MALE);
                        }

                        //TODO: Set student Id
                        //TODO: Set student authentication
                        //TODO: Set student's custom field : school

                    }

                    person.setActive(true);

                    repository.getPersonDao().insertAsync(person, new UmCallback<Long>() {
                        @Override
                        public void onSuccess(Long personPersonUid) {
                            if(personPersonUid != null ){
                                //Done teachers person - create clazzMember

                                repository.getClazzMemberDao().findByPersonUidAndClazzUidAsync(
                                        personPersonUid, thisClazz.getClazzUid(),
                                        new UmCallback<ClazzMember>() {
                                        @Override
                                        public void onSuccess(ClazzMember clazzMember) {
                                            if(clazzMember != null){
                                                //Create one.
                                                ClazzMember teacherClazzMember;
                                                teacherClazzMember = new ClazzMember();
                                                teacherClazzMember.setClazzMemberPersonUid(personPersonUid);
                                                teacherClazzMember.setClazzMemberClazzUid(thisClazz.getClazzUid());
                                                teacherClazzMember.setClazzMemberActive(true);
                                                teacherClazzMember.setDateJoined(System.currentTimeMillis());
                                                teacherClazzMember.setRole(role);

                                                repository.getClazzMemberDao().insertAsync(teacherClazzMember, new UmCallback<Long>() {
                                                    @Override
                                                    public void onSuccess(Long clazzMemberUid) {
                                                        if(clazzMemberUid != null) {
                                                            if (role == ClazzMember.ROLE_TEACHER) {
                                                                checkPerson(thisClazz, bulkLine, ClazzMember.ROLE_STUDENT);
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
                                                System.out.println("ERROR: ClazzMember Exists " +
                                                        "for new Teacher ??");
                                            }
                                        }

                                        @Override
                                        public void onFailure(Throwable exception) {
                                            exception.printStackTrace();
                                        }
                                    }
                                );


                            }else{
                                System.out.println("ERROR: UNABLE TO PERSIST TEACHER!");
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

            }
        });
    }

    public void startBulkUpload(String file){
        readFile(file);
    }

    @Override
    public void setUIStrings() {

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
