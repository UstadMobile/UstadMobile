package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.db.dao.ClazzDao
import com.ustadmobile.core.db.dao.ClazzLogAttendanceRecordDao
import com.ustadmobile.core.db.dao.ClazzLogDao
import com.ustadmobile.core.db.dao.ClazzMemberDao
import com.ustadmobile.core.db.dao.FeedEntryDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.ScheduleDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UmCallbackWithDefaultValue
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ClassLogDetailView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.FeedEntry
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonNameWithClazzName
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ScheduledCheck

import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ABSENT
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ATTENDED
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_PARTIAL
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


/**
 * ClazzLogDetail's presenter - responsible for the logic of displaying (and editing) every
 * Attendance Logs Entry attempt for a clazz and a date. (who's id we get from arguments). This is common
 * with a new Attendance Log Entry.
 *
 */
class ClazzLogDetailPresenter(context: Any,
                              arguments: Map<String, String>?,
                              view: ClassLogDetailView,
                              val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<ClassLogDetailView>(context, arguments!!, view) {

    var isHasEditPermissions = false
        private set

    private var loggedInPersonUid = 0L

    private var currentClazzLogs: List<ClazzLogDao.ClazzLogUidAndDate>? = null

    private var currentClazzLog: ClazzLog? = null
    private var currentSchedule: Schedule? = null

    private var currentClazzLogIndex: Int = 0

    private var title: String? = null

    private var teachers: List<ClazzMember>? = null
    private var clazzName: String? = null

    private var clazzLogAttendanceRecordUmProvider: UmProvider<ClazzLogAttendanceRecordWithPerson>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)
    internal var clazzDao = repository.clazzDao
    private val scheduleDao = repository.scheduleDao

    private val setupFromClazzLogCallback = object : UmCallback<ClazzLog> {
        override fun onFailure(exception: Throwable?) {
           print(exception!!.message)
        }

        override fun onSuccess(clazzLog: ClazzLog?) {
            if (clazzLog != null) {
                currentClazzLog = clazzLog
                insertAllAndSetProvider(clazzLog)
                loadClazzLogListForClazz()
                checkPermissions()

                if (title == null) {
                    GlobalScope.launch {
                        val result = repository.clazzDao.getClazzNameAsync(clazzLog
                                .clazzLogClazzUid)
                        title = "$result " + UstadMobileSystemImpl.instance.getString(
                                MessageID.attendance, context)
                        view.runOnUiThread(Runnable{ view.updateToolbarTitle(title!!) })
                    }
                }

                //Get clazzlog schedule
                currentSchedule = scheduleDao.findByUid(currentClazzLog!!.clazzLogScheduleUid)

                //Get all teachers
                val clazzMemberDao = repository.clazzMemberDao
                teachers = clazzMemberDao.findByClazzUid(currentClazzLog!!.clazzLogClazzUid,
                        ClazzMember.ROLE_TEACHER)

                val clazzDao = repository.clazzDao
                clazzName = clazzDao.findByUid(currentClazzLog!!.clazzLogClazzUid).clazzName

                view.runOnUiThread(Runnable{ updateViewDateHeading() })
            }

        }

    }

    private val setTitleCallback = object : UmCallback<String> {
        override fun onFailure(exception: Throwable?) {
            print(exception!!.message)
        }

        override fun onSuccess(result: String?) {
            title = "$result " + UstadMobileSystemImpl.instance.getString(
                    MessageID.attendance, context)
            view.runOnUiThread(Runnable{ view.updateToolbarTitle(title!!) })
        }

    }

    /**
     * The Presenter's onCreate. This populated the provider and sets it to the View.
     *
     * This will be called when the implementation view is ready.
     * (ie: on Android, this is called in the Activity's onCreateView() )
     *
     * Steps to get there:
     * 1. For a particular Clazz UID and date, we find if an existing ClazzLog exists.
     * 2. If it does not, we create it.
     * 3. We then call insertAllAndSetProvider where it checks the records associated and set the
     * provider.
     *
     * @param savedState This is generally the state which Android resumes this app. This is not
     * the arguments!!. It will most likely be null in a normal application run.
     */
    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

        //Get clazz uid and set it
        if (arguments!!.containsKey(ClassLogDetailView.ARG_CLAZZ_LOG_UID)) {
            val clazzLogUid = arguments[ClassLogDetailView.ARG_CLAZZ_LOG_UID]!!.toLong()
            GlobalScope.launch {
                val clazzLog = repository.clazzLogDao.findByUidAsync(clazzLogUid)
                setupFromClazzLogCallback.onSuccess(clazzLog)
            }
        } else if (arguments!!.containsKey(ClassLogDetailView.ARG_MOST_RECENT_BY_CLAZZ_UID)) {
            val clazzUid = arguments[ClassLogDetailView.ARG_MOST_RECENT_BY_CLAZZ_UID]!!.toLong()
            GlobalScope.launch {
                val clazzLog = repository.clazzLogDao.findMostRecentByClazzUid(clazzUid)
                setupFromClazzLogCallback.onSuccess(clazzLog)
            }
        }

    }

    /**
     * Checks permission to enable and show/hide features on ClazzLogDetail screen.
     */
    fun checkPermissions() {
        val clazzDao = repository.clazzDao
        GlobalScope.launch {
            val result = clazzDao.personHasPermission(loggedInPersonUid,
                    currentClazzLog!!.clazzLogClazzUid, Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT)
            isHasEditPermissions = result
            view.showMarkAllButtons(result!!)
        }
    }

    private fun loadClazzLogListForClazz() { repository.clazzLogDao.getListOfClazzLogUidsAndDatesForClazz(
                currentClazzLog!!.clazzLogClazzUid,
                object : UmCallback<List<ClazzLogDao.ClazzLogUidAndDate>> {
                    override fun onSuccess(result: List<ClazzLogDao.ClazzLogUidAndDate>?) {
                        currentClazzLogs = result
                        currentClazzLogIndex = currentClazzLogs!!.indexOf(
                                ClazzLogDao.ClazzLogUidAndDate(currentClazzLog!!))
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })
    }


    private fun updateViewDateHeading() {
        val currentLogDateDate = Date(currentClazzLog!!.logDate)
        var prettyDate = ""
        if (UMCalendarUtil.isToday(currentLogDateDate)) {
            prettyDate = impl.getString(MessageID.today, context)
        }
        val currentLocale = Locale.getDefault()
        prettyDate += " (" +
                UMCalendarUtil.getPrettyDateFromLong(currentClazzLog!!.logDate, currentLocale) + ")"

        //Add Schedule time to this pretty Date

        //Add time to ClazzLog's date
        if (currentSchedule != null) {
            val startTimeLong = currentSchedule!!.sceduleStartTime
            val endTimeLong = currentSchedule!!.scheduleEndTime
            val formatter = SimpleDateFormat.getTimeInstance(DateFormat.SHORT)

            //start time
            val startMins = startTimeLong / (1000 * 60)
            val cal = Calendar.instance
            cal.set(Calendar.HOUR_OF_DAY, (startMins / 60).toInt())
            cal.set(Calendar.MINUTE, (startMins % 60).toInt())
            val startTime = formatter.format(cal.getTime())

            //end time
            val endMins = endTimeLong / (1000 * 60)
            cal.set(Calendar.HOUR_OF_DAY, (endMins / 60).toInt())
            cal.set(Calendar.MINUTE, (endMins % 60).toInt())
            val endTime = formatter.format(cal.getTime())
            prettyDate = "$prettyDate($startTime - $endTime)"
        }


        view.updateDateHeading(prettyDate)
    }

    /**
     * Common method to insert all attendance records for a clazz log uid and prepare its provider
     * to be set to the view.
     *
     * @param result The ClazzLog for which to insert and get provider data for.
     */
    private fun insertAllAndSetProvider(result: ClazzLog) {

        val clazzLogAttendanceRecordDao = repository.clazzLogAttendanceRecordDao

        clazzLogAttendanceRecordDao.insertAllAttendanceRecords(currentClazzLog!!.clazzLogClazzUid,
                result.clazzLogUid, object : UmCallback<Array<Long>> {
            override fun onSuccess(result2: Array<Long>?) {
                //Get provider
                clazzLogAttendanceRecordUmProvider = repository
                        .clazzLogAttendanceRecordDao
                        .findAttendanceRecordsWithPersonByClassLogId(result.clazzLogUid)
                //Set to view
                view.runOnUiThread({
                    view.setClazzLogAttendanceRecordProvider(
                            clazzLogAttendanceRecordUmProvider!!)
                })
            }

            override fun onFailure(exception: Throwable?) {
                print(exception!!.message)
            }
        })
    }

    /**
     * Goes back a day and updates the view
     */
    fun handleClickGoBackDate() {
        incrementLogInList(-1)
    }

    /**
     * Goes ahead by the given number of day(s) and updates the view
     * @param inc   The amount of days that the should update the view with. Can be negative.
     */
    private fun incrementLogInList(inc: Int) {
        val nextIndex = currentClazzLogIndex + inc
        if (nextIndex >= 0 && nextIndex < currentClazzLogs!!.size)
            repository.clazzLogDao.findByUidAsync(currentClazzLogs!![nextIndex].clazzLogUid,
                    setupFromClazzLogCallback)
    }

    /**
     * Goes forward a day and updates the view
     */
    fun handleClickGoForwardDate() {
        incrementLogInList(1)
    }

    /**
     * Method logic to what happens when we click "Done" on the ClassLogDetail View
     */
    fun handleClickDone() {
        //1. Update Done status on ClazzLog for this clazzLogUid
        val clazzLogDao = repository.clazzLogDao
        val clazzDao = repository.clazzDao
        val clazzMemberDao = repository.clazzMemberDao
        val clazzLogAttendanceRecordDao = repository.clazzLogAttendanceRecordDao
        currentClazzLog!!.setDone(true)

        clazzLogDao.updateDoneForClazzLogAsync(currentClazzLog!!.clazzLogUid,
                object : UmCallback<Int> {
                    override fun onSuccess(result: Int?) {

                        //Get clazzMembers (with their attendance percentages) before update:
                        val beforeList = clazzMemberDao.findByClazzUid(
                                currentClazzLog!!.clazzLogClazzUid, ClazzMember.ROLE_STUDENT)
                        val beforeMap = HashMap<Long, ClazzMember>()
                        for (member in beforeList) {
                            beforeMap[member.clazzMemberUid] = member
                        }

                        //2. Update Attendance percentage for this Clazz:
                        clazzDao.updateAttendancePercentage(currentClazzLog!!.clazzLogClazzUid)

                        //3. Update Attendance percentages for ClazzMember for this clazzUid:
                        clazzMemberDao.updateAttendancePercentages(currentClazzLog!!.clazzLogClazzUid)

                        //4. Update attendance total numbers for present/absent/partial for this Clazz:
                        val numPresent = clazzLogAttendanceRecordDao.getAttedanceStatusCount(
                                currentClazzLog!!.clazzLogUid, STATUS_ATTENDED)
                        val numAbsent = clazzLogAttendanceRecordDao.getAttedanceStatusCount(
                                currentClazzLog!!.clazzLogUid, STATUS_ABSENT)
                        val numPartial = clazzLogAttendanceRecordDao.getAttedanceStatusCount(
                                currentClazzLog!!.clazzLogUid, STATUS_PARTIAL)
                        clazzLogDao.updateClazzAttendanceNumbersAsync(currentClazzLog!!.clazzLogUid,
                                numPresent, numAbsent, numPartial, null!!)

                        /*FEED ENTRIES*/
                        //Create feed entries for this user
                        val newFeedEntries = ArrayList<FeedEntry>()
                        val updateFeedEntries = ArrayList<FeedEntry>()

                        //5. Create feedEntries for the ones that have dropped values.
                        val afterList = clazzMemberDao.findByClazzUid(
                                currentClazzLog!!.clazzLogClazzUid, ClazzMember.ROLE_STUDENT)
                        for (after in afterList) {
                            val before = beforeMap[after.clazzMemberUid]
                            if (before != null
                                    && before.getAttendancePercentage() >= feedAlertPerentageHigh
                                    && after.getAttendancePercentage() < feedAlertPerentageHigh) {
                                //this ClazzMember has fallen below the threshold

                                val personDao = repository.personDao
                                val thisPerson = personDao.findByUid(before.clazzMemberPersonUid)


                                val feedLinkViewPerson = PersonDetailView.VIEW_NAME + "?" +
                                        PersonDetailView.ARG_PERSON_UID + "=" +
                                        after.clazzMemberPersonUid

                                for (teacher in teachers!!) {

                                    val feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                                            teacher.clazzMemberPersonUid, currentClazzLog!!.clazzLogUid,
                                            ScheduledCheck.TYPE_CHECK_ATTENDANCE_VARIATION_HIGH, feedLinkViewPerson)

                                    val existingEntry = repository.feedEntryDao.findByUid(feedEntryUid)
                                    val thisEntry = FeedEntry(
                                            feedEntryUid,
                                            "Attendance dropped",
                                            "Student " + thisPerson!!.firstNames + " " +
                                                    thisPerson.lastName + " of Class " +
                                                    clazzName + " attendance dropped " +
                                                    feedAlertPerentageHigh * 100 + "%",
                                            feedLinkViewPerson,
                                            clazzName!!,
                                            teacher.clazzMemberPersonUid
                                    )

                                    if (existingEntry == null) {
                                        newFeedEntries.add(thisEntry)
                                    } else {
                                        updateFeedEntries.add(thisEntry)
                                    }
                                }
                            }

                            if (before != null
                                    && before.getAttendancePercentage() >= feedAlertPerentageMed
                                    && after.getAttendancePercentage() < feedAlertPerentageMed) {
                                //this ClazzMember has fallen below the threshold

                                val personDao = repository.personDao
                                val thisPerson = personDao.findByUid(before.clazzMemberPersonUid)

                                val feedLinkViewPerson = PersonDetailView.VIEW_NAME + "?" +
                                        PersonDetailView.ARG_PERSON_UID + "=" +
                                        after.clazzMemberPersonUid

                                for (teacher in teachers!!) {
                                    val feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                                            teacher.clazzMemberPersonUid, currentClazzLog!!.clazzLogUid,
                                            ScheduledCheck.TYPE_CHECK_ATTENDANCE_VARIATION_MED, feedLinkViewPerson)

                                    val existingEntry = repository.feedEntryDao.findByUid(feedEntryUid)
                                    val thisEntry = FeedEntry(
                                            feedEntryUid,
                                            "Attendance dropped",
                                            "Student " + thisPerson!!.firstNames + " " +
                                                    thisPerson.lastName + " of Class " +
                                                    clazzName + " attendance dropped " +
                                                    feedAlertPerentageMed * 100 + "%",
                                            feedLinkViewPerson,
                                            clazzName!!,
                                            teacher.clazzMemberPersonUid
                                    )
                                    if (existingEntry == null) {
                                        newFeedEntries.add(thisEntry)
                                    } else {
                                        updateFeedEntries.add(thisEntry)
                                    }
                                }
                            }
                        }

                        repository.feedEntryDao.insertList(newFeedEntries)
                        repository.feedEntryDao.updateList(updateFeedEntries)

                        //6. Create feedEntries for student not partial more than 3 times.
                        clazzMemberDao.findAllMembersForAttendanceOverConsecutiveDays(
                                ClazzLogAttendanceRecord.STATUS_PARTIAL, tardyFrequency,
                                currentClazzLog!!.clazzLogClazzUid,
                                object : UmCallback<List<PersonNameWithClazzName>> {
                                    override fun onSuccess(theseGuys: List<PersonNameWithClazzName>?) {
                                        //Create feed entries for this user
                                        val newEntries = ArrayList<FeedEntry>()
                                        val updateEntries = ArrayList<FeedEntry>()

                                        for (each in theseGuys!!) {

                                            val feedLinkViewPerson = PersonDetailView.VIEW_NAME + "?" +
                                                    PersonDetailView.ARG_PERSON_UID + "=" +
                                                    each.personUid

                                            for (teacher in teachers!!) {
                                                val feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                                                        teacher.clazzMemberPersonUid, currentClazzLog!!.clazzLogUid,
                                                        ScheduledCheck.TYPE_CHECK_PARTIAL_REPETITION_MED, feedLinkViewPerson)

                                                val existingEntry = repository.feedEntryDao.findByUid(feedEntryUid)
                                                val thisEntry = FeedEntry(
                                                        feedEntryUid,
                                                        "Tardy behaviour",
                                                        "Student " + each.firstNames + " " +
                                                                each.lastName + " partially attended Class "
                                                                + clazzName + " over 3 times",
                                                        feedLinkViewPerson,
                                                        clazzName!!,
                                                        teacher.clazzMemberPersonUid
                                                )

                                                if (existingEntry == null) {
                                                    newEntries.add(thisEntry)
                                                } else {
                                                    updateEntries.add(thisEntry)
                                                }
                                            }
                                        }
                                        //Updating/Inserting inside this thread to avoid concurrent modification exception
                                        repository.feedEntryDao.insertList(newEntries)
                                        repository.feedEntryDao.updateList(updateEntries)

                                    }

                                    override fun onFailure(exception: Throwable?) {
                                        print(exception!!.message)
                                    }
                                }
                        )

                        //7. Crate feedEntries for student not attended two classes in a row.
                        clazzMemberDao.findAllMembersForAttendanceOverConsecutiveDays(
                                ClazzLogAttendanceRecord.STATUS_ABSENT, absentFrequencyLow,
                                currentClazzLog!!.clazzLogClazzUid,
                                object : UmCallback<List<PersonNameWithClazzName>> {
                                    override fun onSuccess(theseGuys: List<PersonNameWithClazzName>?) {

                                        //Create feed entries for this user
                                        val newEntries = ArrayList<FeedEntry>()
                                        val updateEntries = ArrayList<FeedEntry>()

                                        for (each in theseGuys!!) {

                                            val feedLinkViewPerson = PersonDetailView.VIEW_NAME + "?" +
                                                    PersonDetailView.ARG_PERSON_UID + "=" +
                                                    each.personUid

                                            //a. Send to teachers
                                            for (teacher in teachers!!) {
                                                val feedEntryUid = FeedEntryDao.generateFeedEntryHash(
                                                        teacher.clazzMemberPersonUid, currentClazzLog!!.clazzLogUid,
                                                        ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_LOW, feedLinkViewPerson)

                                                val existingEntry = repository.feedEntryDao.findByUid(feedEntryUid)
                                                val thisEntry = FeedEntry(
                                                        feedEntryUid,
                                                        "Absent behaviour",
                                                        "Student " + each.firstNames + " " +
                                                                each.lastName + " absent in Class " + clazzName
                                                                + " over " + tardyFrequency + " times",
                                                        feedLinkViewPerson,
                                                        clazzName!!,
                                                        teacher.clazzMemberPersonUid
                                                )

                                                if (existingEntry == null) {
                                                    newEntries.add(thisEntry)
                                                } else {
                                                    updateEntries.add(thisEntry)
                                                }

                                            }
                                        }
                                        //Updating/Inserting inside this thread to avoid concurrent modification exception
                                        repository.feedEntryDao.insertList(newEntries)
                                        repository.feedEntryDao.updateList(updateEntries)
                                    }

                                    override fun onFailure(exception: Throwable?) {
                                        print(exception!!.message)
                                    }
                                })

                        //8. Set any FeedEntry to done
                        repository.feedEntryDao.markEntryAsDoneByClazzLogUidAndTaskType(
                                currentClazzLog!!.clazzLogUid,
                                ScheduledCheck.TYPE_RECORD_ATTENDANCE_REMINDER, true)

                        //9. Close the activity.
                        view.finish()
                    }

                    override fun onFailure(exception: Throwable?) {
                        print(exception!!.message)
                    }
                })
    }


    /**
     * Handle when the user taps to mark all present, or mark all absent. This will update the
     * database to set all ClazzLogAttendanceRecord
     *
     * @param attendanceStatus attendance status to set for all ClazzLogAttendanceRecords that
     * are in this ClazzLog
     */
    fun handleMarkAll(attendanceStatus: Int) {
        repository.clazzLogAttendanceRecordDao
                .updateAllByClazzLogUid(currentClazzLog!!.clazzLogUid,
                        attendanceStatus, null!!)
    }

    /**
     * Handle when the user taps a student and marks it. This will update the database and
     * therefore the recycler view via LiveData.
     *
     * @param clazzLogAttendanceRecordUid       The Attendance Log Entry's per person Record Uid
     * @param attendanceStatus      The Person Attendance Log Entry's attendance status. Can be:
     * STATUS_ATTENDED, STATUS_ABSENT, STATUS_PARTIAL as defined in
     * ClazzLogAttendanceRecord
     */
    fun handleMarkStudent(clazzLogAttendanceRecordUid: Long, attendanceStatus: Int) {
        repository.clazzLogAttendanceRecordDao
                .updateAttendanceStatus(clazzLogAttendanceRecordUid,
                        attendanceStatus, null!!)
    }

    companion object {

        val feedAlertPerentageHigh = 0.69f
        private val feedAlertPerentageMed = 0.5f
        val tardyFrequency = 3
        val absentFrequencyLow = 2
        val absentFrequencyHigh = 30
    }

}
