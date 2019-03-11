package com.ustadmobile.core.scheduler;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.FeedEntryDao;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.FeedEntry;
import com.ustadmobile.lib.db.entities.Location;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.ScheduledCheck;
import com.ustadmobile.test.core.impl.PlatformTestUtil;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestScheduledCheckRunner {

    UmAppDatabase db;

    UmAppDatabase repo;

    private Clazz clazz;

    private ClazzLog clazzLog;

    private Person clazzTeacher;


    protected void initDb() {
        db = UmAppDatabase.getInstance(PlatformTestUtil.getTargetContext());
        db.clearAllTables();
        repo = db.getRepository("http://localhost/dummy/", "");

        Location clazzLocation = new Location("Clazz Location", "Clazz Location",
                "Asia/Beirut");
        clazzLocation.setLocationUid(db.getLocationDao().insert(clazzLocation));
        clazz = new Clazz("A Clazz", clazzLocation.getLocationUid());
        clazz.setClazzUid(db.getClazzDao().insert(clazz));

        clazzTeacher = new Person("teacher", "teacher", "test");
        clazzTeacher.setPersonUid(db.getPersonDao().insert(clazzTeacher));

        clazzLog = new ClazzLog(1, clazz.getClazzUid(), System.currentTimeMillis(), 0);
        clazzLog.setClazzLogUid(db.getClazzLogDao().insert(clazzLog));

        db.getClazzMemberDao().insert(new ClazzMember(clazz.getClazzUid(),
                clazzTeacher.getPersonUid(), ClazzMember.ROLE_TEACHER));
    }

    @Test
    public void givenRecordAttendanceScheduledCheckAttendanceNotDone_whenRun_thenShouldCreateFeedEntry() {
        initDb();
        ScheduledCheck check = new ScheduledCheck(System.currentTimeMillis(),
                ScheduledCheck.TYPE_RECORD_ATTENDANCE_REMINDER,
                ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" + clazzLog.getClazzLogUid());
        check.setScheduledCheckId(db.getScheduledCheckDao().insert(check));

        ScheduledCheckRunner runner = new ScheduledCheckRunner(check, db, repo);
        runner.run();

        String feedLink = ClassLogDetailView.VIEW_NAME + "?" + ClazzListView.ARG_CLAZZ_UID +
                "=" + clazzLog.getClazzLogClazzUid();

        String expectedLink = ClassLogDetailView.VIEW_NAME + "?" + ClazzListView.ARG_CLAZZ_UID +
                "=" + clazzLog.getClazzLogClazzUid();
        FeedEntry feedEntry = db.getFeedEntryDao().findByLink(clazzTeacher.getPersonUid(),
                expectedLink);

        long expectedUid = FeedEntryDao.generateFeedEntryHash(clazzTeacher.getPersonUid(),
                clazzLog.getClazzLogUid(), ScheduledCheck.TYPE_RECORD_ATTENDANCE_REMINDER, feedLink);

        Assert.assertNotNull("Feed entry with correct link for teacher has been created",
                feedEntry);
        Assert.assertEquals("Feed entry has expected UID hash", expectedUid,
                feedEntry.getFeedEntryUid());
    }

    @Test
    public void givenNextDayRecordAttendanceCheck_whenRun_thenShouldCreateFeedEntriesFor() {

        initDb();
        ScheduledCheck check = new ScheduledCheck(System.currentTimeMillis(),
                ScheduledCheck.TYPE_CHECK_ATTENDANCE_NOT_RECORDED_DAY_AFTER,
                ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" + clazzLog.getClazzLogUid());
        check.setScheduledCheckId(db.getScheduledCheckDao().insert(check));

        ScheduledCheckRunner runner = new ScheduledCheckRunner(check, db, repo);
        runner.run();

        FeedEntryDao feedEntryDao = repo.getFeedEntryDao();
        List<FeedEntry> allFeeds = feedEntryDao.findAll();
        Assert.assertNotNull(allFeeds);

    }

}
