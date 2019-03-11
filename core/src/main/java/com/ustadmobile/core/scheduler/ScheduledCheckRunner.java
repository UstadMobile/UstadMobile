package com.ustadmobile.core.scheduler;

import com.ustadmobile.core.db.UmAppDatabase;
import com.ustadmobile.core.db.dao.FeedEntryDao;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.util.UMCalendarUtil;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ClassDetailView;
import com.ustadmobile.core.view.ClassLogDetailView;
import com.ustadmobile.core.view.ClazzListView;
import com.ustadmobile.core.view.PersonDetailView;
import com.ustadmobile.lib.db.entities.Clazz;
import com.ustadmobile.lib.db.entities.ClazzLog;
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord;
import com.ustadmobile.lib.db.entities.ClazzMember;
import com.ustadmobile.lib.db.entities.ClazzMemberWithPerson;
import com.ustadmobile.lib.db.entities.FeedEntry;
import com.ustadmobile.lib.db.entities.Person;
import com.ustadmobile.lib.db.entities.PersonNameWithClazzName;
import com.ustadmobile.lib.db.entities.Role;
import com.ustadmobile.lib.db.entities.Schedule;
import com.ustadmobile.lib.db.entities.ScheduledCheck;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.ustadmobile.core.controller.ClazzLogDetailPresenter.absentFrequencyHigh;
import static com.ustadmobile.core.controller.ClazzLogDetailPresenter.absentFrequencyLow;
import static com.ustadmobile.core.controller.ClazzLogDetailPresenter.feedAlertPerentageHigh;
import static com.ustadmobile.core.controller.ClazzLogDetailPresenter.tardyFrequency;
import static com.ustadmobile.core.view.ClassLogDetailView.ARG_MOST_RECENT_BY_CLAZZ_UID;

public class ScheduledCheckRunner implements Runnable{

    private ScheduledCheck scheduledCheck;

    private UmAppDatabase database;

    private UmAppDatabase dbRepository;

    public ScheduledCheckRunner(ScheduledCheck scheduledCheck, UmAppDatabase database,
                                UmAppDatabase dbRepository) {
        this.scheduledCheck = scheduledCheck;
        this.database = database;
        this.dbRepository = dbRepository;
    }

    @Override
    public void run() {
        if(scheduledCheck == null || scheduledCheck.getCheckParameters() == null){
            return;
        }
        Map<String, String> params = UMFileUtil.parseParams(scheduledCheck.getCheckParameters(),
                ';');


        if(scheduledCheck.getCheckType() == ScheduledCheck.TYPE_RECORD_ATTENDANCE_REMINDER) {

            long clazzLogUid = Long.parseLong(params.get(ScheduledCheck.PARAM_CLAZZ_LOG_UID));
            ClazzLog clazzLog = dbRepository.getClazzLogDao().findByUid(clazzLogUid);
            String clazzName = dbRepository.getClazzDao().getClazzName(
                    clazzLog.getClazzLogClazzUid());


            //We want to send this feed (reminder) when its not done or cancelled (ie not done)
            if(!clazzLog.isDone() || clazzLog.isCanceled()) {

                Schedule lazzLogSchedule =
                        dbRepository.getScheduleDao().findByUid(clazzLog.getClazzLogScheduleUid());

                String timeBit = "";
                if(lazzLogSchedule != null){
                    //Add time to ClazzLog's date
                    long startTimeLong = lazzLogSchedule.getSceduleStartTime();
                    long endTimeLong = lazzLogSchedule.getScheduleEndTime();
                    DateFormat formatter = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);

                    //start time
                    long startMins = startTimeLong / (1000 * 60);
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, (int)(startMins / 60));
                    cal.set(Calendar.MINUTE, (int)(startMins % 60));
                    String startTime = formatter.format(cal.getTime());

                    //end time
                    long endMins = endTimeLong / (1000 * 60);
                    cal.set(Calendar.HOUR_OF_DAY, (int)(endMins / 60));
                    cal.set(Calendar.MINUTE, (int)(endMins % 60));
                    String endTime = formatter.format(cal.getTime());
                    timeBit = ", " + startTime + " - " + endTime;
                }



                List<ClazzMemberWithPerson> teachers = dbRepository.getClazzMemberDao()
                        .findClazzMemberWithPersonByRoleForClazzUidSync(
                                clazzLog.getClazzLogClazzUid(), ClazzMember.ROLE_TEACHER);

                List<FeedEntry> newFeedEntries = new ArrayList<>();
                List<FeedEntry> updateFeedEntries = new ArrayList<>();

                for(ClazzMemberWithPerson teacher : teachers) {

                    //String feedLink = ClassLogDetailView.VIEW_NAME + "?" + ClazzListView.ARG_CLAZZ_UID +
                    //        "=" + clazzLog.getClazzLogClazzUid();
                    String feedLink = ClassLogDetailView.VIEW_NAME + "?" + ARG_MOST_RECENT_BY_CLAZZ_UID +
                            "=" + clazzLog.getClazzLogClazzUid();

                    long feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                            teacher.getClazzMemberPersonUid(), clazzLogUid,
                            ScheduledCheck.TYPE_RECORD_ATTENDANCE_REMINDER, feedLink);

                    FeedEntry thisEntry = new FeedEntry(feedEntryUid, "Record attendance",
                            "Record attendance for class " + clazzName + " (" +
                                    UMCalendarUtil.getPrettyDateSimpleFromLong(clazzLog.getLogDate())
                                    + timeBit + ")",
                            feedLink,
                            clazzName,
                            teacher.getClazzMemberPersonUid());
                    FeedEntry existingEntry = dbRepository.getFeedEntryDao().findByUid(feedEntryUid);
                    if(existingEntry != null){
                        updateFeedEntries.add(thisEntry);
                    }else{
                        newFeedEntries.add(thisEntry);
                    }

                }
                dbRepository.getFeedEntryDao().insertList(newFeedEntries);
                dbRepository.getFeedEntryDao().updateList(updateFeedEntries);
            }
        }

        //If absent repetition for officer.
        if(scheduledCheck.getCheckType() == ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER){
            long clazzLogUid = Long.parseLong(params.get(ScheduledCheck.PARAM_CLAZZ_LOG_UID));
            ClazzLog currentClazzLog = dbRepository.getClazzLogDao().findByUid(clazzLogUid);
            if(currentClazzLog != null) {

                String clazzName = dbRepository.getClazzDao().getClazzName(
                        currentClazzLog.getClazzLogClazzUid());

                //Get officers
                Role officerRole = dbRepository.getRoleDao().findByNameSync(Role.ROLE_NAME_OFFICER);
                List<Person> officers = new ArrayList<>();
                if (officerRole != null) {
                    officers = dbRepository.getClazzDao().findPeopleWithRoleAssignedToClazz(
                            currentClazzLog.getClazzLogClazzUid(), officerRole.getRoleUid());
                }

                //If condition meets:
                List<Person> finalOfficers = officers;
                dbRepository.getClazzMemberDao().findAllMembersForAttendanceOverConsecutiveDays(
                        ClazzLogAttendanceRecord.STATUS_ABSENT, absentFrequencyLow,
                        currentClazzLog.getClazzLogClazzUid(), new UmCallback<List<PersonNameWithClazzName>>() {
                            @Override
                            public void onSuccess(List<PersonNameWithClazzName> theseGuys) {
                                //Create feed entries for this user for every teacher
                                List<FeedEntry> newFeedEntries = new ArrayList<>();
                                List<FeedEntry> updateFeedEntries = new ArrayList<>();
                                for (PersonNameWithClazzName each : theseGuys) {

                                    String feedLinkViewPerson = PersonDetailView.VIEW_NAME + "?" +
                                            PersonDetailView.ARG_PERSON_UID + "=" +
                                            String.valueOf(each.getPersonUid());

                                    //Send to Officer as well.
                                    for (Person officer : finalOfficers) {

                                        long feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                                                officer.getPersonUid(), currentClazzLog.getClazzLogUid(),
                                                ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER, feedLinkViewPerson);


                                        FeedEntry thisEntry = new FeedEntry(
                                                feedEntryUid,
                                                "Absent behaviour",
                                                "Student " + each.getFirstNames() + " " +
                                                        each.getLastName() + " absent in Class " + clazzName
                                                        + " over " + tardyFrequency + " times",
                                                feedLinkViewPerson,
                                                clazzName,
                                                officer.getPersonUid());

                                        FeedEntry existingEntry = dbRepository.getFeedEntryDao().findByUid(feedEntryUid);
                                        if(existingEntry != null){
                                            updateFeedEntries.add(thisEntry);
                                        }else{
                                            newFeedEntries.add(thisEntry);
                                        }
                                    }
                                }

                                dbRepository.getFeedEntryDao().insertList(newFeedEntries);
                                dbRepository.getFeedEntryDao().updateList(updateFeedEntries);

                            }

                            @Override
                            public void onFailure(Throwable exception) {
                                exception.printStackTrace();
                            }
                        });
            }
        }

        if(scheduledCheck.getCheckType() == ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_TIME_HIGH){

            long clazzLogUid = Long.parseLong(params.get(ScheduledCheck.PARAM_CLAZZ_LOG_UID));
            ClazzLog currentClazzLog = dbRepository.getClazzLogDao().findByUid(clazzLogUid);
            if(currentClazzLog != null) {
                String clazzName = dbRepository.getClazzDao().getClazzName(
                        currentClazzLog.getClazzLogClazzUid());


                //Get M&E officers
                Role mneOfficerRole = dbRepository.getRoleDao().findByNameSync(Role.ROLE_NAME_MNE);
                List<Person> mneofficers = new ArrayList<>();
                if (mneOfficerRole != null) {
                    mneofficers = dbRepository.getClazzDao().findPeopleWithRoleAssignedToClazz(
                            currentClazzLog.getClazzLogClazzUid(), mneOfficerRole.getRoleUid());
                }

                //9. MNE An alert when a student has not attended a single day in a month(dropout)
                List<Person> finalMneofficers = mneofficers;
                dbRepository.getClazzMemberDao().findAllMembersForAttendanceOverConsecutiveDays(
                        ClazzLogAttendanceRecord.STATUS_ABSENT, absentFrequencyHigh,
                        currentClazzLog.getClazzLogClazzUid(), new UmCallback<List<PersonNameWithClazzName>>() {
                            @Override
                            public void onSuccess(List<PersonNameWithClazzName> theseGuys) {

                                //Create feed entries for this user for every teacher
                                List<FeedEntry> newFeedEntries = new ArrayList<>();
                                List<FeedEntry> updateFeedEntries = new ArrayList<>();
                                for (PersonNameWithClazzName each : theseGuys) {

                                    //Feed link
                                    String feedLinkViewPerson = PersonDetailView.VIEW_NAME + "?" +
                                            PersonDetailView.ARG_PERSON_UID + "=" +
                                            String.valueOf(each.getPersonUid());

                                    for (Person mne : finalMneofficers) {
                                        //Feed uid
                                        long feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                                                mne.getPersonUid(), currentClazzLog.getClazzLogUid(),
                                                ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_TIME_HIGH,
                                                feedLinkViewPerson);

                                        FeedEntry thisEntry = new FeedEntry(
                                                feedEntryUid,
                                                "Student dropout",
                                                "Student " + each.getFirstNames() + " " +
                                                        each.getLastName() + " absent in Class "
                                                        + clazzName + " over 30 days"
                                                ,
                                                feedLinkViewPerson,
                                                clazzName,
                                                mne.getPersonUid()
                                        );

                                        FeedEntry existingEntry = dbRepository.getFeedEntryDao().findByUid(feedEntryUid);
                                        if(existingEntry != null){
                                            updateFeedEntries.add(thisEntry);
                                        }else{
                                            newFeedEntries.add(thisEntry);
                                        }
                                    }
                                }
                                dbRepository.getFeedEntryDao().insertList(newFeedEntries);
                                dbRepository.getFeedEntryDao().updateList(updateFeedEntries);
                            }

                            @Override
                            public void onFailure(Throwable exception) {
                                exception.printStackTrace();
                            }
                        });
            }
        }

        //Check attendance not taken the next day.
        if(scheduledCheck.getCheckType() == ScheduledCheck.TYPE_CHECK_ATTENDANCE_NOT_RECORDED_DAY_AFTER){
            long clazzLogUid = Long.parseLong(params.get(ScheduledCheck.PARAM_CLAZZ_LOG_UID));
            ClazzLog currentClazzLog = dbRepository.getClazzLogDao().findByUid(clazzLogUid);
            if(currentClazzLog != null) {
                Clazz currentClazz = dbRepository.getClazzDao().findByUid(currentClazzLog.getClazzLogClazzUid());
                String clazzName = currentClazz.getClazzName();

                //Get officers
                Role officerRole = dbRepository.getRoleDao().findByNameSync(Role.ROLE_NAME_OFFICER);
                List<Person> officers = new ArrayList<>();
                if(officerRole != null){
                    officers = dbRepository.getClazzDao().findPeopleWithRoleAssignedToClazz(
                            currentClazzLog.getClazzLogClazzUid(), officerRole.getRoleUid());
                }
                //Get teachers
                List<ClazzMemberWithPerson> teachers = dbRepository.getClazzMemberDao()
                        .findClazzMemberWithPersonByRoleForClazzUidSync(
                                currentClazzLog.getClazzLogClazzUid(), ClazzMember.ROLE_TEACHER);
                //Get M&E officers
                Role mneOfficerRole = dbRepository.getRoleDao().findByNameSync(Role.ROLE_NAME_MNE);
                List<Person> mneofficers = new ArrayList<>();
                if(mneOfficerRole != null){
                    mneofficers = dbRepository.getClazzDao().findPeopleWithRoleAssignedToClazz(
                            currentClazzLog.getClazzLogClazzUid(), mneOfficerRole.getRoleUid());
                }
                //Get Admins
                List<Person> admins = dbRepository.getPersonDao().findAllAdminsAsList();

                //Build a list of new feed entries to be added.
                List<FeedEntry> newFeedEntries = new ArrayList<>();
                List<FeedEntry> updateFeedEntries = new ArrayList<>();

                String feedLinkViewClass = ClassDetailView.VIEW_NAME + "?" +
                        ClazzListView.ARG_CLAZZ_UID + "=" +
                        currentClazzLog.getClazzLogClazzUid();

                for(ClazzMemberWithPerson teacher:teachers){
                    long feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                            teacher.getPerson().getPersonUid(), currentClazzLog.getClazzLogUid(),
                            ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH, feedLinkViewClass);

                    FeedEntry thisEntry = new FeedEntry(feedEntryUid, "Record attendance (overdue)",
                            "No attendance recorded for class. ",
                            feedLinkViewClass,
                            clazzName,
                            teacher.getClazzMemberPersonUid());

                    FeedEntry existingEntry = dbRepository.getFeedEntryDao().findByUid(feedEntryUid);
                    if(existingEntry != null){
                        updateFeedEntries.add(thisEntry);
                    }else{
                        newFeedEntries.add(thisEntry);
                    }

                }

                for(Person officer:officers){
                    long feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                            officer.getPersonUid(), currentClazzLog.getClazzLogUid(),
                            ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH, feedLinkViewClass);

                    FeedEntry thisEntry = new FeedEntry(feedEntryUid, "Record attendance (overdue)",
                            "No attendance recorded for class. ",
                            feedLinkViewClass,
                            clazzName,
                            officer.getPersonUid());

                    FeedEntry existingEntry = dbRepository.getFeedEntryDao().findByUid(feedEntryUid);
                    if(existingEntry != null){
                        updateFeedEntries.add(thisEntry);
                    }else{
                        newFeedEntries.add(thisEntry);
                    }
                }

                for(Person mne:mneofficers){
                    long feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                            mne.getPersonUid(), currentClazzLog.getClazzLogUid(),
                            ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH, feedLinkViewClass);

                    FeedEntry thisEntry = new FeedEntry(feedEntryUid, "Record attendance (overdue)",
                            "No attendance recorded for class. ",
                            feedLinkViewClass,
                            clazzName,
                            mne.getPersonUid());

                    FeedEntry existingEntry = dbRepository.getFeedEntryDao().findByUid(feedEntryUid);
                    if(existingEntry != null){
                        updateFeedEntries.add(thisEntry);
                    }else{
                        newFeedEntries.add(thisEntry);
                    }
                }

                for(Person admin:admins){
                    long feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                            admin.getPersonUid(), currentClazzLog.getClazzLogUid(),
                            ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH, feedLinkViewClass);

                    FeedEntry thisEntry = new FeedEntry(feedEntryUid, "Record attendance (overdue)",
                            "No attendance recorded for class. ",
                            feedLinkViewClass,
                            clazzName,
                            admin.getPersonUid());

                    FeedEntry existingEntry = dbRepository.getFeedEntryDao().findByUid(feedEntryUid);
                    if(existingEntry != null){
                        updateFeedEntries.add(thisEntry);
                    }else{
                        newFeedEntries.add(thisEntry);
                    }

                }

                dbRepository.getFeedEntryDao().insertList(newFeedEntries);
                dbRepository.getFeedEntryDao().updateList(updateFeedEntries);
            }

        }

        if(scheduledCheck.getCheckType() == ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH){

            long clazzLogUid = Long.parseLong(params.get(ScheduledCheck.PARAM_CLAZZ_LOG_UID));
            ClazzLog currentClazzLog = dbRepository.getClazzLogDao().findByUid(clazzLogUid);
            if(currentClazzLog != null){
                Clazz currentClazz = dbRepository.getClazzDao().findByUid(currentClazzLog.getClazzLogClazzUid());
                String clazzName = currentClazz.getClazzName();

                //Get officers
                Role officerRole = dbRepository.getRoleDao().findByNameSync(Role.ROLE_NAME_OFFICER);
                List<Person> officers = new ArrayList<>();
                if(officerRole != null){
                    officers = dbRepository.getClazzDao().findPeopleWithRoleAssignedToClazz(
                            currentClazzLog.getClazzLogClazzUid(), officerRole.getRoleUid());
                }


                //Officer to get an alert when student has been absent 2 or more days in a row
                float clazzBeforeAttendance = dbRepository.getClazzDao().findClazzAttendancePercentageWithoutLatestClazzLog(
                        currentClazz.getClazzUid());
                float clazzAttendance = currentClazz.getAttendanceAverage();

                if(clazzBeforeAttendance >=  feedAlertPerentageHigh &&
                        clazzAttendance < feedAlertPerentageHigh){


                    List<FeedEntry> newFeedEntries = new ArrayList<>();
                    List<FeedEntry> updateFeedEntries = new ArrayList<>();

                    String feedLinkViewClass = ClassDetailView.VIEW_NAME + "?" +
                            ClazzListView.ARG_CLAZZ_UID + "=" +
                            currentClazzLog.getClazzLogClazzUid();

                    for(Person officer:officers){
                        long feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                                officer.getPersonUid(), currentClazzLog.getClazzLogUid(),
                                ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH, feedLinkViewClass);

                        FeedEntry thisEntry =  new FeedEntry(
                                feedEntryUid,
                                "Class average dropped",
                                "Class " + clazzName + " dropped attendance  " +
                                        String.valueOf(feedAlertPerentageHigh * 100)  +"%"
                                ,
                                feedLinkViewClass,
                                clazzName,
                                officer.getPersonUid()
                        );

                        FeedEntry existingEntry = dbRepository.getFeedEntryDao().findByUid(feedEntryUid);
                        if(existingEntry != null){
                            updateFeedEntries.add(thisEntry);
                        }else{
                            newFeedEntries.add(thisEntry);
                        }
                    }
                    dbRepository.getFeedEntryDao().insertList(newFeedEntries);
                    dbRepository.getFeedEntryDao().updateList(updateFeedEntries);
                }
            }
        }


        //delete this item from database - no longer needed
        database.getScheduledCheckDao().deleteCheck(scheduledCheck);
    }


}
