package com.ustadmobile.staging.core.scheduler

import com.ustadmobile.core.controller.ClazzLogDetailPresenter.Companion.absentFrequencyHigh
import com.ustadmobile.core.controller.ClazzLogDetailPresenter.Companion.absentFrequencyLow
import com.ustadmobile.core.controller.ClazzLogDetailPresenter.Companion.feedAlertPerentageHigh
import com.ustadmobile.core.controller.ClazzLogDetailPresenter.Companion.tardyFrequency
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.FeedEntryDao
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.ClassLogDetailView
import com.ustadmobile.core.view.ClassLogDetailView.Companion.ARG_MOST_RECENT_BY_CLAZZ_UID
import com.ustadmobile.core.view.ClazzListView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


class ScheduledCheckRunner(private val scheduledCheck: ScheduledCheck?,
                           private val database: UmAppDatabase,
                           private val dbRepository: UmAppDatabase) : Runnable {

    override fun run() {
        if (scheduledCheck == null || scheduledCheck.checkParameters == null) {
            return
        }
        val params = UMFileUtil.parseParams(scheduledCheck.checkParameters!!,
                ';')


        if (scheduledCheck.checkType == ScheduledCheck.TYPE_RECORD_ATTENDANCE_REMINDER) {

            val clazzLogUid = (params[ScheduledCheck.PARAM_CLAZZ_LOG_UID]!!).toLong()
            val clazzLog = dbRepository.clazzLogDao.findByUid(clazzLogUid)
            val clazzName = dbRepository.clazzDao.getClazzName(
                    clazzLog!!.clazzLogClazzUid)


            //We want to send this feed (reminder) when its not done or cancelled (ie not done)
            if (!clazzLog!!.clazzLogDone || clazzLog.clazzLogCancelled) {

                val lazzLogSchedule = dbRepository.scheduleDao.findByUid(clazzLog.clazzLogScheduleUid)

                var timeBit = ""
                if (lazzLogSchedule != null) {
                    //Add time to ClazzLog's date
                    val startTimeLong = lazzLogSchedule.sceduleStartTime
                    val endTimeLong = lazzLogSchedule.scheduleEndTime

                    val startTime = UMCalendarUtil.showTimeForGivenLongDate(startTimeLong)
                    val endTime = UMCalendarUtil.showTimeForGivenLongDate(endTimeLong)

                    timeBit = ", $startTime - $endTime"
                }

                val teachers = dbRepository.clazzMemberDao
                        .findClazzMemberWithPersonByRoleForClazzUidSync(
                                clazzLog.clazzLogClazzUid, ClazzMember.ROLE_TEACHER)

                val newFeedEntries = ArrayList<FeedEntry>()
                val updateFeedEntries = ArrayList<FeedEntry>()

                for (teacher in teachers) {

                    //String feedLink = ClassLogDetailView.VIEW_NAME + "?" + ClazzListView.ARG_CLAZZ_UID +
                    //        "=" + clazzLog.getClazzLogClazzUid();
                    val feedLink = ClassLogDetailView.VIEW_NAME + "?" + ARG_MOST_RECENT_BY_CLAZZ_UID +
                            "=" + clazzLog.clazzLogClazzUid

                    val feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                            teacher.clazzMemberPersonUid, clazzLogUid,
                            ScheduledCheck.TYPE_RECORD_ATTENDANCE_REMINDER, feedLink)

                    val thisEntry = FeedEntry(feedEntryUid, "Record attendance",
                            "Record attendance for class " + clazzName + " (" +
                                    UMCalendarUtil.getPrettyDateSimpleFromLong(clazzLog.logDate, "")
                                    + timeBit + ")",
                            feedLink,
                            clazzName!!,
                            teacher.clazzMemberPersonUid)
                    thisEntry.dateCreated = UMCalendarUtil.getDateInMilliPlusDays(0)
                    val existingEntry = dbRepository.feedEntryDao.findByUid(feedEntryUid)
                    if (existingEntry != null) {
                        updateFeedEntries.add(thisEntry)
                    } else {
                        newFeedEntries.add(thisEntry)
                    }

                }
                dbRepository.feedEntryDao.insertList(newFeedEntries)
                dbRepository.feedEntryDao.updateList(updateFeedEntries)
            }
        }

        //If absent repetition for officer.
        if (scheduledCheck.checkType == ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER) {
            val clazzLogUid = (params[ScheduledCheck.PARAM_CLAZZ_LOG_UID]!!).toLong()
            val currentClazzLog = dbRepository.clazzLogDao.findByUid(clazzLogUid)
            if (currentClazzLog != null) {

                val clazzName = dbRepository.clazzDao.getClazzName(
                        currentClazzLog.clazzLogClazzUid)

                //Get officers
                val officerRole = dbRepository.roleDao.findByNameSync(Role.ROLE_NAME_OFFICER)
                var officers: List<Person> = ArrayList()
                if (officerRole != null) {
                    officers = dbRepository.clazzDao.findPeopleWithRoleAssignedToClazz(
                            currentClazzLog.clazzLogClazzUid, officerRole.roleUid)
                }

                //If condition meets:
                val finalOfficers = officers
                GlobalScope.launch {
                    val theseGuys = dbRepository.clazzMemberDao.findAllMembersForAttendanceOverConsecutiveDays(
                            ClazzLogAttendanceRecord.STATUS_ABSENT, absentFrequencyLow,
                            currentClazzLog.clazzLogClazzUid)
                    //Create feed entries for this user for every teacher
                    val newFeedEntries = ArrayList<FeedEntry>()
                    val updateFeedEntries = ArrayList<FeedEntry>()
                    for (each in theseGuys!!) {

                        val feedLinkViewPerson = PersonDetailView.VIEW_NAME + "?" +
                                PersonDetailView.ARG_PERSON_UID + "=" +
                                each.personUid.toString()

                        //Send to Officer as well.
                        for (officer in finalOfficers) {

                            val feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                                    officer.personUid, currentClazzLog.clazzLogUid,
                                    ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER, feedLinkViewPerson)


                            val thisEntry = FeedEntry(
                                    feedEntryUid,
                                    "Absent behaviour",
                                    "Student " + each.firstNames + " " +
                                            each.lastName + " absent in Class " + clazzName
                                            + " over " + tardyFrequency + " times",
                                    feedLinkViewPerson,
                                    clazzName!!,
                                    officer.personUid)
                            thisEntry.dateCreated = UMCalendarUtil.getDateInMilliPlusDays(0)
                            val existingEntry = dbRepository.feedEntryDao.findByUid(feedEntryUid)
                            if (existingEntry != null) {
                                updateFeedEntries.add(thisEntry)
                            } else {
                                newFeedEntries.add(thisEntry)
                            }
                        }
                    }

                    dbRepository.feedEntryDao.insertList(newFeedEntries)
                    dbRepository.feedEntryDao.updateList(updateFeedEntries)
                }
            }
        }

        if (scheduledCheck.checkType == ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_TIME_HIGH) {

            val clazzLogUid = (params[ScheduledCheck.PARAM_CLAZZ_LOG_UID]!!).toLong()
            val currentClazzLog = dbRepository.clazzLogDao.findByUid(clazzLogUid)
            if (currentClazzLog != null) {
                val clazzName = dbRepository.clazzDao.getClazzName(
                        currentClazzLog.clazzLogClazzUid)


                //Get M&E officers
                val mneOfficerRole = dbRepository.roleDao.findByNameSync(Role.ROLE_NAME_MNE)
                var mneofficers: List<Person> = ArrayList()
                if (mneOfficerRole != null) {
                    mneofficers = dbRepository.clazzDao.findPeopleWithRoleAssignedToClazz(
                            currentClazzLog.clazzLogClazzUid, mneOfficerRole.roleUid)
                }

                //9. MNE An alert when a student has not attended a single day in a month(dropout)
                val finalMneofficers = mneofficers
                GlobalScope.launch {
                    val theseGuys = dbRepository.clazzMemberDao
                            .findAllMembersForAttendanceOverConsecutiveDays(
                                    ClazzLogAttendanceRecord.STATUS_ABSENT, absentFrequencyHigh,
                                    currentClazzLog.clazzLogClazzUid)

                    //Create feed entries for this user for every teacher
                    val newFeedEntries = ArrayList<FeedEntry>()
                    val updateFeedEntries = ArrayList<FeedEntry>()
                    for (each in theseGuys!!) {

                        //Feed link
                        val feedLinkViewPerson = PersonDetailView.VIEW_NAME + "?" +
                                PersonDetailView.ARG_PERSON_UID + "=" +
                                each.personUid.toString()

                        for (mne in finalMneofficers) {
                            //Feed uid
                            val feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                                    mne.personUid, currentClazzLog.clazzLogUid,
                                    ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_TIME_HIGH,
                                    feedLinkViewPerson)

                            val thisEntry = FeedEntry(
                                    feedEntryUid,
                                    "Student dropout",
                                    "Student " + each.firstNames + " " +
                                            each.lastName + " absent in Class "
                                            + clazzName + " over 30 days",
                                    feedLinkViewPerson,
                                    clazzName!!,
                                    mne.personUid
                            )
                            thisEntry.dateCreated = UMCalendarUtil.getDateInMilliPlusDays(0)
                            val existingEntry = dbRepository.feedEntryDao.findByUid(feedEntryUid)
                            if (existingEntry != null) {
                                updateFeedEntries.add(thisEntry)
                            } else {
                                newFeedEntries.add(thisEntry)
                            }
                        }
                    }
                    dbRepository.feedEntryDao.insertList(newFeedEntries)
                    dbRepository.feedEntryDao.updateList(updateFeedEntries)
                }
            }
        }

        //Check attendance not taken the next day.
        if (scheduledCheck.checkType == ScheduledCheck.TYPE_CHECK_ATTENDANCE_NOT_RECORDED_DAY_AFTER) {
            val clazzLogUid = (params[ScheduledCheck.PARAM_CLAZZ_LOG_UID]!!).toLong()
            val currentClazzLog = dbRepository.clazzLogDao.findByUid(clazzLogUid)
            if (currentClazzLog != null) {
                val currentClazz = dbRepository.clazzDao.findByUid(currentClazzLog.clazzLogClazzUid)
                val clazzName = currentClazz!!.clazzName

                //Get officers
                val officerRole = dbRepository.roleDao.findByNameSync(Role.ROLE_NAME_OFFICER)
                var officers: List<Person> = ArrayList()
                if (officerRole != null) {
                    officers = dbRepository.clazzDao.findPeopleWithRoleAssignedToClazz(
                            currentClazzLog.clazzLogClazzUid, officerRole.roleUid)
                }
                //Get teachers
                val teachers = dbRepository.clazzMemberDao
                        .findClazzMemberWithPersonByRoleForClazzUidSync(
                                currentClazzLog.clazzLogClazzUid, ClazzMember.ROLE_TEACHER)
                //Get M&E officers
                val mneOfficerRole = dbRepository.roleDao.findByNameSync(Role.ROLE_NAME_MNE)
                var mneofficers: List<Person> = ArrayList()
                if (mneOfficerRole != null) {
                    mneofficers = dbRepository.clazzDao.findPeopleWithRoleAssignedToClazz(
                            currentClazzLog.clazzLogClazzUid, mneOfficerRole.roleUid)
                }
                //Get Admins
                val admins = dbRepository.personDao.findAllAdminsAsList()

                //Build a list of new feed entries to be added.
                val newFeedEntries = ArrayList<FeedEntry>()
                val updateFeedEntries = ArrayList<FeedEntry>()

                val clazzLogSchedule = dbRepository.scheduleDao.findByUid(currentClazzLog.clazzLogScheduleUid)

                var timeBit = ""
                if (clazzLogSchedule != null) {
                    //Add time to ClazzLog's date
                    val startTimeLong = clazzLogSchedule.sceduleStartTime
                    val endTimeLong = clazzLogSchedule.scheduleEndTime

                    val startTime = UMCalendarUtil.showTimeForGivenLongDate(startTimeLong)
                    val endTime = UMCalendarUtil.showTimeForGivenLongDate(endTimeLong)

                    timeBit = ", $startTime - $endTime"
                }

                val feedLinkViewClass = ClazzDetailView.VIEW_NAME + "?" +
                        ClazzListView.ARG_CLAZZ_UID + "=" +
                        currentClazzLog.clazzLogClazzUid
                val feedLinkDesc = ("No attendance recorded for class " + clazzName + " (" +
                        UMCalendarUtil.getPrettyDateSimpleFromLong(currentClazzLog.logDate, "")
                        + timeBit + ")")
                val feedLinkTitle = "Record attendance (overdue)"

                for (teacher in teachers) {
                    if (teacher.person!!.personUid != 0L) {
                        val feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                                teacher.person!!.personUid, currentClazzLog.clazzLogUid,
                                ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH, feedLinkViewClass)

                        val thisEntry = FeedEntry(feedEntryUid, feedLinkTitle,
                                feedLinkDesc,
                                feedLinkViewClass,
                                clazzName!!,
                                teacher.clazzMemberPersonUid)
                        thisEntry.dateCreated = UMCalendarUtil.getDateInMilliPlusDays(0)
                        thisEntry.deadline = currentClazzLog.logDate

                        val existingEntry = dbRepository.feedEntryDao.findByUid(feedEntryUid)
                        if (existingEntry != null) {
                            updateFeedEntries.add(thisEntry)
                        } else {
                            newFeedEntries.add(thisEntry)
                        }
                    }

                }

                for (officer in officers) {
                    if (officer.personUid != 0L) {

                        val feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                                officer.personUid, currentClazzLog.clazzLogUid,
                                ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH, feedLinkViewClass)

                        val thisEntry = FeedEntry(feedEntryUid, feedLinkTitle,
                                feedLinkDesc,
                                feedLinkViewClass,
                                clazzName!!,
                                officer.personUid)
                        thisEntry.dateCreated = UMCalendarUtil.getDateInMilliPlusDays(0)
                        thisEntry.deadline = currentClazzLog.logDate

                        val existingEntry = dbRepository.feedEntryDao.findByUid(feedEntryUid)
                        if (existingEntry != null) {
                            updateFeedEntries.add(thisEntry)
                        } else {
                            newFeedEntries.add(thisEntry)
                        }
                    }
                }

                for (mne in mneofficers) {
                    if (mne.personUid != 0L) {

                        val feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                                mne.personUid, currentClazzLog.clazzLogUid,
                                ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH, feedLinkViewClass)

                        val thisEntry = FeedEntry(feedEntryUid, feedLinkTitle,
                                feedLinkDesc,
                                feedLinkViewClass,
                                clazzName!!,
                                mne.personUid)
                        thisEntry.dateCreated = UMCalendarUtil.getDateInMilliPlusDays(0)
                        thisEntry.deadline = currentClazzLog.logDate

                        val existingEntry = dbRepository.feedEntryDao.findByUid(feedEntryUid)
                        if (existingEntry != null) {
                            updateFeedEntries.add(thisEntry)
                        } else {
                            newFeedEntries.add(thisEntry)
                        }
                    }
                }

                for (admin in admins) {
                    if (admin.personUid != 0L) {
                        val feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                                admin.personUid, currentClazzLog.clazzLogUid,
                                ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH, feedLinkViewClass)

                        val thisEntry = FeedEntry(feedEntryUid, feedLinkTitle,
                                feedLinkDesc,
                                feedLinkViewClass,
                                clazzName!!,
                                admin.personUid)
                        thisEntry.dateCreated = UMCalendarUtil.getDateInMilliPlusDays(0)
                        thisEntry.deadline = currentClazzLog.logDate

                        val existingEntry = dbRepository.feedEntryDao.findByUid(feedEntryUid)
                        if (existingEntry != null) {
                            updateFeedEntries.add(thisEntry)
                        } else {
                            newFeedEntries.add(thisEntry)
                        }
                    }
                }

                dbRepository.feedEntryDao.insertList(newFeedEntries)
                dbRepository.feedEntryDao.updateList(updateFeedEntries)
            }

        }

        if (scheduledCheck.checkType == ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH) {

            val clazzLogUid = params[ScheduledCheck.PARAM_CLAZZ_LOG_UID]!!.toString().toLong()
            val currentClazzLog = dbRepository.clazzLogDao.findByUid(clazzLogUid)
            if (currentClazzLog != null) {
                val currentClazz = dbRepository.clazzDao.findByUid(currentClazzLog.clazzLogClazzUid)
                val clazzName = currentClazz!!.clazzName

                //Get officers
                val officerRole = dbRepository.roleDao.findByNameSync(Role.ROLE_NAME_OFFICER)
                var officers: List<Person> = ArrayList()
                if (officerRole != null) {
                    officers = dbRepository.clazzDao.findPeopleWithRoleAssignedToClazz(
                            currentClazzLog.clazzLogClazzUid, officerRole.roleUid)
                }


                //Officer to get an alert when student has been absent 2 or more days in a row
                val clazzBeforeAttendance = dbRepository.clazzDao.findClazzAttendancePercentageWithoutLatestClazzLog(
                        currentClazz.clazzUid)
                val clazzAttendance = currentClazz.attendanceAverage

                if (clazzBeforeAttendance != null) {
                    if (clazzBeforeAttendance >= feedAlertPerentageHigh
                            && clazzAttendance < feedAlertPerentageHigh) {


                        val newFeedEntries = ArrayList<FeedEntry>()
                        val updateFeedEntries = ArrayList<FeedEntry>()

                        val feedLinkViewClass = ClazzDetailView.VIEW_NAME + "?" +
                                ClazzListView.ARG_CLAZZ_UID + "=" +
                                currentClazzLog.clazzLogClazzUid

                        for (officer in officers) {
                            val feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                                    officer.personUid, currentClazzLog.clazzLogUid,
                                    ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH, feedLinkViewClass)

                            val thisEntry = FeedEntry(
                                    feedEntryUid,
                                    "Class average dropped",
                                    "Class " + clazzName + " dropped attendance  " +
                                            (feedAlertPerentageHigh * 100).toString() + "%",
                                    feedLinkViewClass,
                                    clazzName!!,
                                    officer.personUid
                            )
                            thisEntry.dateCreated = UMCalendarUtil.getDateInMilliPlusDays(0)
                            val existingEntry = dbRepository.feedEntryDao.findByUid(feedEntryUid)
                            if (existingEntry != null) {
                                updateFeedEntries.add(thisEntry)
                            } else {
                                newFeedEntries.add(thisEntry)
                            }
                        }
                        dbRepository.feedEntryDao.insertList(newFeedEntries)
                        dbRepository.feedEntryDao.updateList(updateFeedEntries)
                    }
                }
            }
        }


        //delete this item from database - no longer needed
        database.scheduledCheckDao.deleteCheck(scheduledCheck)
    }


}
