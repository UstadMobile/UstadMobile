package com.ustadmobile.core.controller;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzLogDao;
import com.ustadmobile.core.db.dao.PersonAuthDao;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonAuth;
import com.ustadmobile.lib.db.entities.Schedule;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;

import static com.ustadmobile.test.core.util.CoreTestUtil.TEST_URI;
import static com.ustadmobile.test.core.util.CoreTestUtil.startServer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class TestClazzLogDetailPresenter {

    UstadMobileSystemImpl mainImpl;

    UstadMobileSystemImpl systemImplSpy;

    private HttpServer server;

    private UmAppDatabase db;

    private UmAppDatabase repo;

    private static final String VALID_USER = "testuser";
    private static final String VALID_PASS = "secret";

    private long testClazzUid;

    List<ClazzMember> clazzMembers;

    private ClassLogDetailView mockView;


    @Before
    public void setUp() {
        mainImpl = UstadMobileSystemImpl.getInstance();
        systemImplSpy = Mockito.spy(mainImpl);
        UstadMobileSystemImpl.setMainInstance(systemImplSpy);
        server = startServer();

        db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        repo = db.getRepository(TEST_URI, "");

        db.clearAllTables();
        Person testPerson = new Person();
        testPerson.setUsername(VALID_USER);
        long personUid = repo.getPersonDao().insert(testPerson);

        PersonAuth testPersonAuth = new PersonAuth(personUid,
                PersonAuthDao.ENCRYPTED_PASS_PREFIX +
                        PersonAuthDao.encryptPassword(VALID_PASS));
        repo.getPersonAuthDao().insert(testPersonAuth);


        //Create location
        Location testLocation = new Location("Location A", "Test Location",
                TimeZone.getDefault().getID());
        repo.getLocationDao().insert(testLocation);

        //Create clazz
        Clazz testClazz = new Clazz("Test Class A", testLocation.getLocationUid());
        testClazzUid = repo.getClazzDao().insert(testClazz);


        //Create students for Clazz
        for(int i=0;i<5;i++){
            Person thisPerson = new Person("user" + i, "Test"+i,
                    "Person"+i);
            repo.getPersonDao().insert(thisPerson);

            ClazzMember thisStudent = new ClazzMember(testClazz.getClazzUid(),
                    thisPerson.getPersonUid(), ClazzMember.ROLE_STUDENT);
            repo.getClazzMemberDao().insert(thisStudent);

        }

        //Create teacher for this clazz
        Person teacherPerson = new Person("teachera", "TeacherA", "Person");
        repo.getPersonDao().insert(teacherPerson);

        ClazzMember thisTeacher = new ClazzMember(testClazz.getClazzUid(),
                teacherPerson.getPersonUid(), ClazzMember.ROLE_TEACHER);
        repo.getClazzMemberDao().insert(thisTeacher);

        //Create clazz schedule
        Schedule classSchedule = new Schedule();
        classSchedule.setScheduleClazzUid(testClazzUid);
        classSchedule.setScheduleActive(true);
        classSchedule.setSceduleStartTime(600000);  //10 minutes ie 0:10
        classSchedule.setScheduleEndTime(4200000);  //70 minutes ie 1:10

        //Create clazz log for 5 days
        repo.getScheduleDao().createClazzLogsForEveryDayFromDays(5,
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

    /**
     * Condition:   Attendance for one Student marked Absent consecutively
     * Action:      Done clicked for the current open ClazzLog
     * Result:      Should create alert of absence over 2 days
     */
    @Test
    public void givenWhenAttendanceForOneStudentMarkedTwiceAbsentConsecutively_whenDoneClicked_thenShouldCreateFeedEntry(){

        //Create arguments needed for the presenter
        Hashtable args = new Hashtable();
        args.put(ClassLogDetailView.ARG_MOST_RECENT_BY_CLAZZ_UID, testClazzUid);



    }

    @Test
    public void givenValidUsernameAndPassword_whenHandleLoginCalled_thenShouldCallSystemImplGo() {
        Hashtable args = new Hashtable();
        args.put(Login2Presenter.ARG_NEXT, "somewhere");


        ClazzLogDao clazzLogDao = repo.getClazzLogDao();
        List<ClazzLog> clazzLogsDesc = clazzLogDao.findByClazzUidAsList(testClazzUid);
        for(ClazzLog clazzLog:clazzLogsDesc){

        }
//        Login2Presenter presenter = new Login2Presenter(PlatformTestUtil.getTargetContext(),
//                args, mockView);
//        presenter.handleClickLogin(VALID_USER, VALID_PASS, TEST_URI);
//
//
//        verify(systemImplSpy, timeout(5000)).go("somewhere",
//                PlatformTestUtil.getTargetContext());
//
//        UmAccount activeAccount = UmAccountManager.getActiveAccount(
//                PlatformTestUtil.getTargetContext());
//        Assert.assertNotNull(activeAccount);
    }
}
