package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.EntityRoleDao;
import com.ustadmobile.core.db.dao.PersonAuthDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonGroupDao;
import com.ustadmobile.core.db.dao.PersonGroupMemberDao;
import com.ustadmobile.core.db.dao.RoleDao;
import com.ustadmobile.core.db.dao.ScheduleDao;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;
import com.ustadmobile.lib.db.entities.PersonGroup;
import com.ustadmobile.lib.db.entities.PersonGroupMember;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.entities.Schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.ustadmobile.lib.db.entities.Role.ROLE_NAME_TEACHER;

public class SetUpUtil {

    private UmAppDatabase repo;

    SetUpUtil(UmAppDatabase repo){
        this.repo = repo;
    }

    public List<Long> createPerson(HashMap<String, String> people){

        List<Long> peopleUids = new ArrayList<>();
        List<Long> groupUids = new ArrayList<>();

        Iterator<Map.Entry<String, String>> peopleIterator = people.entrySet().iterator();
        while(peopleIterator.hasNext()){
            Map.Entry<String, String> next = peopleIterator.next();
            String username = next.getKey();
            String password = next.getValue();

            PersonDao personDao = repo.getPersonDao();

            //Insert person
            Person thisPerson = new Person();
            thisPerson.setUsername(username);
            long personUid = personDao.insert(thisPerson);

            //Create auth for person
            PersonAuth testPersonAuth = new PersonAuth(personUid,
                    PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                            PersonAuthDao.encryptPassword(password));
            long personAuthUid = repo.getPersonAuthDao().insert(testPersonAuth);

            //Create personGroup
            PersonGroup thisPersonGroup = new PersonGroup();
            thisPersonGroup.setGroupName(thisPerson.getFirstNames() + " group");
            PersonGroupDao personGroupDao = repo.getPersonGroupDao();
            thisPersonGroup.setGroupUid(personGroupDao.insert(thisPersonGroup));
            long personGroupUid = thisPersonGroup.getGroupUid();

            //Add person to thisPersonGroup
            PersonGroupMember thisPersonGroupMember = new PersonGroupMember();
            thisPersonGroupMember.setGroupMemberPersonUid(personUid);
            thisPersonGroupMember.setGroupMemberGroupUid(personGroupUid);
            PersonGroupMemberDao personGroupMemberDao = repo.getPersonGroupMemberDao();
            thisPersonGroupMember.setGroupMemberUid(personGroupMemberDao.insert(thisPersonGroupMember));


            peopleUids.add(personUid);
            groupUids.add(personGroupUid);

        }
        return peopleUids;
    }

    public List<Long> createLocation(HashMap<String, Long> locations, String timeZone){

        List<Long> locationUids = new ArrayList<>();
        Iterator<Map.Entry<String, Long>> locationIterator = locations.entrySet().iterator();
        while(locationIterator.hasNext()){
            Map.Entry<String, Long> next = locationIterator.next();
            String locationName = next.getKey();
            Long parentLocation = next.getValue();

            //Create location
            Location testLocation = new Location(locationName, locationName, timeZone);
            testLocation.setLocationUid(repo.getLocationDao().insert(testLocation));

            locationUids.add(testLocation.getLocationUid());
        }

        return locationUids;
    }

    public List<Long> addClazz(List<String> clazzNames, long locationUid){

        List<Long> clazzUids = new ArrayList<>();
        for(String clazzName : clazzNames){
            Clazz testClazz = new Clazz(clazzName, locationUid);
            long clazzUid = repo.getClazzDao().insert(testClazz);
            clazzUids.add(clazzUid);
        }

        return clazzUids;
    }

    public List<Long> addClazz(List<String> clazzNames, String newLocationName, String timeZone){
        //Create location
        Location testLocation = new Location(newLocationName, newLocationName, timeZone);
        testLocation.setLocationUid(repo.getLocationDao().insert(testLocation));

        return addClazz(clazzNames, testLocation.getLocationUid());
    }


    public void addRandomClazzMember(long testClazzUid, int numberOfStudents, int numberOfTeachers){

        //Create students for Clazz
        for(int i=0;i<numberOfStudents;i++){
            Random random = new Random();
            int rand = random.nextInt(900) + 100;
            String username = "student" + rand + "_" +  i;
            String password = random.nextInt(900) + 100 + "_pass";
            createClazzMember(testClazzUid, username, password, username, username,
                    ClazzMember.ROLE_STUDENT);

        }

        //Create students for Clazz
        for(int i=0;i<numberOfTeachers;i++){
            Random random = new Random();
            int rand = random.nextInt(900) + 100;
            String username = "teacher" + rand + "_" +  i;
            String password = random.nextInt(900) + 100 + "_pass";

            createClazzMember(testClazzUid, username, password, username, username,
                    ClazzMember.ROLE_TEACHER);

        }
    }

    public List<Long> addClazzAndMembers(List<String> clazzNames, String newLocationName, String timeZone,
                                   int numStudents, int numTeachers){

        List<Long> clazzUids = addClazz(clazzNames, newLocationName, timeZone);
        for(Long everyClazzUid : clazzUids){
            addRandomClazzMember(everyClazzUid, numStudents, numTeachers);
        }
        return clazzUids;
    }

    public long getTeacherRole(){
        //Create teacher Role
        RoleDao roleDao = repo.getRoleDao();
        Role teacherRole = roleDao.findByNameSync(ROLE_NAME_TEACHER);

        long teacherRoleUid;

        if (teacherRole == null) {
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
                            Role.PERMISSION_PERSON_SELECT |                //See People
                            Role.PERMISSION_PERSON_PICTURE_INSERT |         //Insert Person Picture
                            Role.PERMISSION_PERSON_PICTURE_SELECT |         //See Person Picture
                            Role.PERMISSION_PERSON_PICTURE_UPDATE           //Update Person picture
                    ;
            newRole.setRolePermissions(teacherPermissions);
            newRole.setRoleUid(roleDao.insert(newRole));
            teacherRoleUid = newRole.getRoleUid();
        }else{
            teacherRoleUid = teacherRole.getRoleUid();
        }

        return  teacherRoleUid;
    }



    public long createClazzMember(long clazzUid, String username, String password,
                                  String firstName, String lastName, int clazzRole){


        PersonDao personDao = repo.getPersonDao();

        //Create the person
        Person thisPerson = new Person(username, firstName, lastName);
        long personUid = personDao.insert(thisPerson);
        thisPerson.setPersonUid(personUid);

        //Create auth for person
        PersonAuth testPersonAuth = new PersonAuth(personUid,
                PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                        PersonAuthDao.encryptPassword(password));
        long personAuthUid = repo.getPersonAuthDao().insert(testPersonAuth);

        //Create personGroup
        PersonGroup thisPersonGroup = new PersonGroup();
        thisPersonGroup.setGroupName(thisPerson.getFirstNames() + " group");
        PersonGroupDao personGroupDao = repo.getPersonGroupDao();
        thisPersonGroup.setGroupUid(personGroupDao.insert(thisPersonGroup));
        long personGroupUid = thisPersonGroup.getGroupUid();

        //Add person to thisPersonGroup
        PersonGroupMember thisPersonGroupMember = new PersonGroupMember();
        thisPersonGroupMember.setGroupMemberPersonUid(personUid);
        thisPersonGroupMember.setGroupMemberGroupUid(personGroupUid);
        PersonGroupMemberDao personGroupMemberDao = repo.getPersonGroupMemberDao();
        thisPersonGroupMember.setGroupMemberUid(personGroupMemberDao.insert(thisPersonGroupMember));

        long roleUid;
        //Create EntityRole for that Clazz and Role
        if(clazzRole == ClazzMember.ROLE_TEACHER){
            roleUid = getTeacherRole();

            EntityRoleDao entityRoleDao = repo.getEntityRoleDao();
            EntityRole entityRole = new EntityRole();
            entityRole.setErTableId(Clazz.TABLE_ID);
            entityRole.setErEntityUid(clazzUid);
            entityRole.setErRoleUid(roleUid);
            entityRoleDao.insert(entityRole);

            //For a specific clazz
            EntityRole newEntityClazzSpecific = new EntityRole();
            newEntityClazzSpecific.setErGroupUid(personGroupUid);
            newEntityClazzSpecific.setErRoleUid(roleUid);
            newEntityClazzSpecific.setErTableId(Clazz.TABLE_ID);
            newEntityClazzSpecific.setErEntityUid(clazzUid);
            entityRoleDao.insert(newEntityClazzSpecific);
        }



        //Create ClazzMember
        ClazzMember thisStudent = new ClazzMember(clazzUid,
                thisPerson.getPersonUid(), clazzRole);
        long clazzMemberUid = repo.getClazzMemberDao().insert(thisStudent);


        return clazzMemberUid;

    }

    public void createRandomClazzSchedule(long clazzUid){
        ScheduleDao scheduleDao = repo.getScheduleDao();
        //Create clazz schedule - Everyday
        for(int i = Schedule.DAY_SUNDAY; i<=Schedule.DAY_SATURDAY; i++){
            Schedule classSchedule = new Schedule();
            classSchedule.setScheduleClazzUid(clazzUid);
            classSchedule.setScheduleActive(true);
            classSchedule.setSceduleStartTime(600000);  //10 minutes ie 0:10
            classSchedule.setScheduleEndTime(4200000);  //70 minutes ie 1:10
            classSchedule.setScheduleFrequency(i);
            classSchedule.setScheduleDay(Schedule.DAY_MONDAY);
            long classScheduleUid = scheduleDao.insert(classSchedule);
            classSchedule.setScheduleUid(classScheduleUid);
        }
    }
}
