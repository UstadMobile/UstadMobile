package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao;
import com.ustadmobile.core.db.dao.ClazzLogDao;
import com.ustadmobile.core.db.dao.EntityRoleDao;
import com.ustadmobile.core.db.dao.FeedEntryDao;
import com.ustadmobile.core.db.dao.PersonAuthDao;
import com.ustadmobile.core.db.dao.PersonDao;
import com.ustadmobile.core.db.dao.PersonGroupDao;
import com.ustadmobile.core.db.dao.PersonGroupMemberDao;
import com.ustadmobile.core.db.dao.RoleDao;
import com.ustadmobile.core.db.dao.ScheduleDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.core.view.Login2View;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.FeedEntry;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;
import com.ustadmobile.lib.db.entities.PersonGroup;
import com.ustadmobile.lib.db.entities.PersonGroupMember;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.entities.Schedule;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;

import static com.ustadmobile.lib.db.entities.Role.ROLE_NAME_TEACHER;
import static com.ustadmobile.test.core.util.CoreTestUtil.TEST_URI;
import static com.ustadmobile.test.core.util.CoreTestUtil.startServer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

public class TestClazzLogDetailPresenter {

    UstadMobileSystemImpl mainImpl;

    UstadMobileSystemImpl systemImplSpy;

    private HttpServer server;

    private UmAppDatabase db;

    private UmAppDatabase repo;

    private static final String VALID_USER = "testuser";
    public static final String TEACHER_USER = "teachera";
    private static final String VALID_PASS = "secret";

    private long testClazzUid;

    List<ClazzMember> clazzMembers;

    private long teacherRoleUid = 0L;
    private long teacherPersonUid = 0L;
    private long teacherPersonGroupUid = 0L;

    private ClassLogDetailView mockView;
    private Login2View loginView;

    public static final int TARDY_CLAZZMEMBER_POSITION = 2;


    @Before
    public void setUp() {
        mainImpl = UstadMobileSystemImpl.getInstance();
        systemImplSpy = Mockito.spy(mainImpl);
        UstadMobileSystemImpl.setMainInstance(systemImplSpy);
        server = startServer();

        db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        repo = db.getRepository(TEST_URI, "");

        db.clearAllTables();
        PersonDao personDao = repo.getPersonDao();
        Person testPerson = new Person();
        testPerson.setUsername(VALID_USER);
        long personUid = personDao.insert(testPerson);

        PersonAuth testPersonAuth = new PersonAuth(personUid,
                PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                        PersonAuthDao.encryptPassword(VALID_PASS));
        repo.getPersonAuthDao().insert(testPersonAuth);


        //Create location
        Location testLocation = new Location("Location A", "Test Location",
                TimeZone.getDefault().getID());
        testLocation.setLocationUid(repo.getLocationDao().insert(testLocation));

        //Create clazz
        Clazz testClazz = new Clazz("Test Class A", testLocation.getLocationUid());
        testClazzUid = repo.getClazzDao().insert(testClazz);


        clazzMembers = new ArrayList<>();
        //Create students for Clazz

        for(int i=0;i<5;i++){
            Person thisPerson = new Person("user" + i, "Test"+i,
                    "Person"+i);
            thisPerson.setPersonUid(personDao.insert(thisPerson));

            ClazzMember thisStudent = new ClazzMember(testClazzUid,
                    thisPerson.getPersonUid(), ClazzMember.ROLE_STUDENT);
            repo.getClazzMemberDao().insert(thisStudent);

            clazzMembers.add(thisStudent);

        }


        //TEACHER

        //Create teacher Role
        RoleDao  roleDao = repo.getRoleDao();
        Role teacherRole = roleDao.findByNameSync(ROLE_NAME_TEACHER);

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


        //Create teacher person
        Person teacherPerson = new Person(TEACHER_USER, "TeacherA", "Person");
        teacherPerson.setPersonUid(personDao.insert(teacherPerson));
        teacherPersonUid = teacherPerson.getPersonUid();

        //Create teacher PersonGroup
        PersonGroup teacherPersonGroup = new PersonGroup();
        teacherPersonGroup.setGroupName(teacherPerson.getFirstNames() + " group");
        PersonGroupDao personGroupDao = repo.getPersonGroupDao();
        teacherPersonGroup.setGroupUid(personGroupDao.insert(teacherPersonGroup));
        teacherPersonGroupUid = teacherPersonGroup.getGroupUid();

        //Add teacher to teacherPersonGroup
        PersonGroupMember teacherPersonGroupMember = new PersonGroupMember();
        teacherPersonGroupMember.setGroupMemberPersonUid(teacherPersonUid);
        teacherPersonGroupMember.setGroupMemberGroupUid(teacherPersonGroupUid);
        PersonGroupMemberDao personGroupMemberDao = repo.getPersonGroupMemberDao();
        teacherPersonGroupMember.setGroupMemberUid(personGroupMemberDao.insert(teacherPersonGroupMember));


        EntityRoleDao entityRoleDao = repo.getEntityRoleDao();
        //Create EntityRole for that Clazz and Teacher
        EntityRole entityRole = new EntityRole();
        entityRole.setErTableId(Clazz.TABLE_ID);
        entityRole.setErEntityUid(testClazzUid);
        entityRole.setErRoleUid(teacherRoleUid);

        entityRoleDao.insert(entityRole);

        //For a specific clazz
        EntityRole newEntityClazzSpecific = new EntityRole();
        newEntityClazzSpecific.setErGroupUid(teacherPersonGroupUid);
        newEntityClazzSpecific.setErRoleUid(teacherRoleUid);
        newEntityClazzSpecific.setErTableId(Clazz.TABLE_ID);
        newEntityClazzSpecific.setErEntityUid(testClazzUid);
        entityRoleDao.insert(newEntityClazzSpecific);

        //Create ClazzMember
        ClazzMember thisTeacher = new ClazzMember(testClazz.getClazzUid(),
                teacherPersonUid, ClazzMember.ROLE_TEACHER);
        repo.getClazzMemberDao().insert(thisTeacher);

        //Set teacher password
        PersonAuth teacherAuth = new PersonAuth(teacherPersonUid,
                PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                        PersonAuthDao.encryptPassword(VALID_PASS));
        repo.getPersonAuthDao().insert(teacherAuth);



        ScheduleDao scheduleDao = repo.getScheduleDao();
        //Create clazz schedule - Everyday
        for(int i=Schedule.DAY_SUNDAY; i<=Schedule.DAY_SATURDAY; i++){
            Schedule classSchedule = new Schedule();
            classSchedule.setScheduleClazzUid(testClazzUid);
            classSchedule.setScheduleActive(true);
            classSchedule.setSceduleStartTime(600000);  //10 minutes ie 0:10
            classSchedule.setScheduleEndTime(4200000);  //70 minutes ie 1:10
            classSchedule.setScheduleFrequency(i);
            classSchedule.setScheduleDay(Schedule.DAY_MONDAY);
            long classScheduleUid = scheduleDao.insert(classSchedule);
            classSchedule.setScheduleUid(classScheduleUid);
        }



        //Create clazz log for 5 days
        scheduleDao.createClazzLogsForEveryDayFromDays(5,
                teacherPerson.getPersonUid(), db);

        mockView = Mockito.mock(ClassLogDetailView.class);
        doAnswer((invocationOnMock) -> {
            new Thread(((Runnable)invocationOnMock.getArgument(0))).start();
            return null;
        }).when(mockView).runOnUiThread(any());

    }

    @After
    public void tearDown() {
        UstadMobileSystemImpl.setMainInstance(mainImpl);
        systemImplSpy = null;
        server.shutdownNow();
    }

    public void logTeacherIn(){
        loginView = Mockito.mock(Login2View.class);
        doAnswer((invocationOnMock) -> {
            new Thread(((Runnable)invocationOnMock.getArgument(0))).start();
            return null;
        }).when(loginView).runOnUiThread(any());

        //Log the teacher in
        Hashtable teacherArgs = new Hashtable();
        teacherArgs.put(Login2Presenter.ARG_NEXT, "somewhere");
        Login2Presenter loginPresenter = new Login2Presenter(PlatformTestUtil.getTargetContext(),
                teacherArgs, loginView);
        loginPresenter.handleClickLogin(TEACHER_USER, VALID_PASS, TEST_URI);

        UmAccount activeAccount =
                UmAccountManager.getActiveAccount(PlatformTestUtil.getTargetContext());
        if(activeAccount != null){
            int x;
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Condition:   Attendance for one Student marked Absent consecutively
     * Action:      Done clicked for the current open ClazzLog
     * Result:      Should create alert of absence over 2 days
     */
    @Test
    public void givenWhenAttendanceForOneStudentMarkedTwiceAbsentConsecutively_whenDoneClicked_thenShouldCreateFeedEntry(){


        logTeacherIn();


        //Create arguments needed for the presenter
        Hashtable args = new Hashtable();
        args.put(ClassLogDetailView.ARG_MOST_RECENT_BY_CLAZZ_UID, testClazzUid);

        long tardyClazzMember = clazzMembers.get(TARDY_CLAZZMEMBER_POSITION).getClazzMemberUid();

        ClazzLogDao clazzLogDao = repo.getClazzLogDao();
        List<ClazzLog> clazzLogsDesc = clazzLogDao.findByClazzUidAsList(testClazzUid);
        for(ClazzLog clazzLog:clazzLogsDesc){
            //Create attendance record for these clazzlogs
            ClazzLogAttendanceRecordDao attendanceRecordDao = repo.getClazzLogAttendanceRecordDao();
            attendanceRecordDao.insertAllAttendanceRecords(clazzLog.getClazzLogClazzUid(),
                clazzLog.getClazzLogUid(), null);

            //Set tardy person
            attendanceRecordDao.updateAttendanceStatusByClazzMemberAndClazzLog(
                    tardyClazzMember, clazzLog.getClazzLogClazzUid(),
                    ClazzLogAttendanceRecord.STATUS_PARTIAL);
        }

        ClazzLogDetailPresenter presenter = new ClazzLogDetailPresenter(PlatformTestUtil.getTargetContext(),
                args, mockView);

        presenter.onCreate(args);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        presenter.handleClickDone();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Now check database.

        FeedEntryDao feedEntryDao = repo.getFeedEntryDao();
        List<FeedEntry> allFeeds = feedEntryDao.findAll();
        Assert.assertNotNull(allFeeds);

    }
}
