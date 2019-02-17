package com.ustadmobile.lib.db;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzDao;
import com.ustadmobile.core.db.dao.ClazzLogDao;
import com.ustadmobile.core.db.dao.ScheduleDao;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.EntityRole;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.entities.Schedule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.List;

public class TestClazzLogDao extends AbstractDaoTest{

    @Before
    public void setUp() {
        initDb();
    }

    protected void addScheduleToMyClazz() {
        Schedule schedule1 = new Schedule();
        schedule1.setSceduleStartTime(13 * 60 * 60 * 1000);    //13 hours - 1 pm ?
        schedule1.setScheduleDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        schedule1.setScheduleActive(true);
        schedule1.setScheduleClazzUid(myClazz.getClazzUid());
        schedule1.setScheduleEndTime(60 * 60 * 1000);          //1 hour

        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/", "");

        ScheduleDao scheduleDao = serverDummyRepo.getScheduleDao();
        scheduleDao.insert(schedule1);
    }

    protected void grantTeacherRoleOnMyClazzToAccountPerson() {
        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/",
                "");
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
                        Role.PERMISSION_PERSON_UPDATE |                 //Update people
                        Role.PERMISSION_PERSON_INSERT;
        Role teacherRole = new Role("teacher", teacherPermissions);
        teacherRole.setRoleUid(serverDummyRepo.getRoleDao().insert(teacherRole));

        EntityRole entityRole = new EntityRole(Clazz.TABLE_ID, myClazz.getClazzUid(),
                accountPersonGroup.getGroupUid(), teacherRole.getRoleUid());
        serverDummyRepo.getEntityRoleDao().insert(entityRole);

    }

    @Test
    public void givenClazzLogInsertedByAccountWithPermission_whenSynced_thenShouldBeOnServer() {
        grantTeacherRoleOnMyClazzToAccountPerson();

        clientDb.syncWith(clientRepo, accountPerson.getPersonUid(),
                100, 100);

        //Make a ClazzLog
        ClazzLog clazzLog = new ClazzLog();
        clazzLog.setClazzLogClazzUid(myClazz.getClazzUid());
        clazzLog.setClazzLogUid(clientRepo.getClazzLogDao().insert(clazzLog));

        clientDb.getClazzLogDao().syncWith(clientRepo.getClazzLogDao(), accountPerson.getPersonUid(),
                100, 100);

        Assert.assertNotNull(serverDb.getClazzLogDao().findByUid(clazzLog.getClazzLogUid()));
    }

    @Test
    public void givenScheduledClazzLogNotExisting_whenCreateClazzLogsCalled_thenClazzLogShouldExist() {
        grantTeacherRoleOnMyClazzToAccountPerson();
        addScheduleToMyClazz();

        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/", "");
        ScheduleDao scheduleDao = serverDummyRepo.getScheduleDao();

        scheduleDao.createClazzLogs(UMCalendarUtil.getDateInMilliPlusDays(0),
                UMCalendarUtil.getDateInMilliPlusDays(1),accountPerson.getPersonUid(),
                serverDummyRepo);

        ClazzLogDao clazzLogDao = serverDummyRepo.getClazzLogDao();
        List<ClazzLog> allClazzLogs = clazzLogDao.findAll();
        Assert.assertEquals("One class log created", 1, allClazzLogs.size());
    }

    @Test
    public void givenScheduledClazzLogExisting_whenCreateClazzLogsCalled_thenShouldNotCreateAnyMoreLogs() {
        grantTeacherRoleOnMyClazzToAccountPerson();
        addScheduleToMyClazz();

        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/", "");
        ScheduleDao scheduleDao = serverDummyRepo.getScheduleDao();
        ClazzLogDao clazzLogDao = serverDummyRepo.getClazzLogDao();

        scheduleDao.createClazzLogs(UMCalendarUtil.getDateInMilliPlusDays(0),
                UMCalendarUtil.getDateInMilliPlusDays(1),accountPerson.getPersonUid(),
                serverDummyRepo);

        int numLogsBefore = clazzLogDao.findAll().size();

        scheduleDao.createClazzLogs(UMCalendarUtil.getDateInMilliPlusDays(0),
                UMCalendarUtil.getDateInMilliPlusDays(1),accountPerson.getPersonUid(),
                serverDummyRepo);

        Assert.assertEquals("No new logs created after log was already created", numLogsBefore,
                clazzLogDao.findAll().size());
    }

}
