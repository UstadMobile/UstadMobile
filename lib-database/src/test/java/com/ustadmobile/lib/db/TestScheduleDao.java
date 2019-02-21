package com.ustadmobile.lib.db;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.ClazzLogDao;
import com.ustadmobile.core.db.dao.ScheduleDao;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.Schedule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.List;

public class TestScheduleDao extends AbstractDaoTest{

    @Before
    public void setUp() {
        initDb();
    }


    protected void addScheduleToMyClazz() {
        Schedule schedule1 = new Schedule();
        schedule1.setSceduleStartTime(13 * 60 * 60 * 1000);    //13 hours - 1 pm
        schedule1.setScheduleDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        schedule1.setScheduleActive(true);
        schedule1.setScheduleClazzUid(myClazz.getClazzUid());
        schedule1.setScheduleEndTime(14 * 60 * 60 * 1000);          //14 hours - 2pm

        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/", "");

        ScheduleDao scheduleDao = serverDummyRepo.getScheduleDao();
        scheduleDao.insert(schedule1);
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
