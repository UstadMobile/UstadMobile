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
import java.util.TimeZone;

public class TestScheduleDao extends AbstractDaoTest{


    @Before
    public void setUp() {
        initDb();
    }


    protected Schedule addScheduleToMyClazz(int startHour, int finishHour) {
        Schedule schedule1 = new Schedule();
        schedule1.setSceduleStartTime(startHour * 60 * 60 * 1000);    //13 hours - 1 pm
        schedule1.setScheduleDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        schedule1.setScheduleActive(true);
        schedule1.setScheduleClazzUid(myClazz.getClazzUid());
        schedule1.setScheduleEndTime(finishHour * 60 * 60 * 1000);          //14 hours - 2pm

        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/", "");

        ScheduleDao scheduleDao = serverDummyRepo.getScheduleDao();
        scheduleDao.insert(schedule1);
        return schedule1;
    }

    @Test
    public void givenScheduledClazzLogNotExisting_whenCreateClazzLogsCalled_thenClazzLogShouldExist() {
        grantTeacherRoleOnMyClazzToAccountPerson();
        Schedule mySchedule = addScheduleToMyClazz(13, 14);

        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/", "");
        ScheduleDao scheduleDao = serverDummyRepo.getScheduleDao();

        scheduleDao.createClazzLogs(UMCalendarUtil.getDateInMilliPlusDays(0),
                UMCalendarUtil.getDateInMilliPlusDays(1),accountPerson.getPersonUid(),
                serverDummyRepo);

        ClazzLogDao clazzLogDao = serverDummyRepo.getClazzLogDao();
        List<ClazzLog> allClazzLogs = clazzLogDao.findAll();
        Assert.assertEquals("One class log created", 1, allClazzLogs.size());
        long startTimeMs = allClazzLogs.get(0).getLogDate();

        Calendar startTimeCal = Calendar.getInstance();
        startTimeCal.setTimeZone(TimeZone.getTimeZone(myClazzLocation.getTimeZone()));
        startTimeCal.setTimeInMillis(startTimeMs);

        long timeSinceStartOfDayInMillis = ((startTimeCal.get(Calendar.HOUR_OF_DAY) * 60) +
                startTimeCal.get(Calendar.MINUTE)) * 60 * 1000;
        Assert.assertEquals("Start time is 13:00 hours", 13,
                startTimeCal.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals("Start time mins is 0", 0,
                startTimeCal.get(Calendar.MINUTE));
        Assert.assertEquals("Start time of ClazzLog is correct number of milliseconds from start of " +
                        "day as per timezone of location",
                mySchedule.getSceduleStartTime(),
                timeSinceStartOfDayInMillis);
    }

    @Test
    public void givenScheduledClazzLogExisting_whenCreateClazzLogsCalled_thenShouldNotCreateAnyMoreLogs() {
        grantTeacherRoleOnMyClazzToAccountPerson();
        addScheduleToMyClazz(13, 14);

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

    @Test
    public void givenClazzScheduledTwiceOnSameDay_whenCreateClazzLogsCalled_thenShouldMakeTwoLogs() {
        grantTeacherRoleOnMyClazzToAccountPerson();
        addScheduleToMyClazz(13, 14);
        addScheduleToMyClazz(15, 16);

        UmAppDatabase serverDummyRepo = serverDb.getRepository("http://localhost/dummy/", "");
        ScheduleDao scheduleDao = serverDummyRepo.getScheduleDao();

        scheduleDao.createClazzLogs(UMCalendarUtil.getDateInMilliPlusDays(0),
                UMCalendarUtil.getDateInMilliPlusDays(1),accountPerson.getPersonUid(),
                serverDummyRepo);

        ClazzLogDao clazzLogDao = serverDummyRepo.getClazzLogDao();
        List<ClazzLog> allClazzLogs = clazzLogDao.findAll();
        Assert.assertEquals("Two class logs created", 2, allClazzLogs.size());
    }

}
