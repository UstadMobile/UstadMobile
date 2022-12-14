package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.util.safeStringify
import com.ustadmobile.core.view.ClazzLogEditAttendanceView
import com.ustadmobile.core.view.ClazzLogEditAttendanceView.Companion.ARG_NEW_CLAZZLOG
import com.ustadmobile.core.view.ClazzLogEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ABSENT
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ATTENDED
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_PARTIAL
import com.ustadmobile.lib.util.copyOnWriteListOf
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI
import kotlin.jvm.Volatile


class ClazzLogEditAttendancePresenter(context: Any,
                          arguments: Map<String, String>, view: ClazzLogEditAttendanceView,
                          di: DI,
                          lifecycleOwner: LifecycleOwner)
    : UstadEditPresenter<ClazzLogEditAttendanceView, ClazzLog>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private val attendanceRecordOneToManyJoinHelper = DefaultOneToManyJoinEditHelper(ClazzLogAttendanceRecord::clazzLogAttendanceRecordUid,
            "state_ClazzLogAttendanceRecord_list",
            ListSerializer(ClazzLogAttendanceRecordWithPerson.serializer()),
            ListSerializer(ClazzLogAttendanceRecordWithPerson.serializer()), this, di,
            ClazzLogAttendanceRecordWithPerson::class) { clazzLogAttendanceRecordUid = it }

    @Volatile
    private var currentClazzLogUid: Long = 0

    /**
     * The user can switch between days using this screen. We need to keep an in memory copy of the
     * any data that may have been modified (clazzLogs and clazzAttendanceRecords). This allows us
     * to commit it to the database if and when the user chooses to save, or discard it if they don't.
     */
    private val clazzLogs: MutableList<ClazzLog> = copyOnWriteListOf()

    private val clazzAttendanceRecords: MutableList<ClazzLogAttendanceRecordWithPerson> = copyOnWriteListOf()

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        currentClazzLogUid = savedState?.get(STATE_CURRENT_UID)?.toLong()
            ?: arguments[ARG_ENTITY_UID]?.toLong() ?: 0
        super.onCreate(savedState)

        view.clazzLogAttendanceRecordList = attendanceRecordOneToManyJoinHelper.liveList
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzLog? {
        val newClazzLog = arguments[ARG_NEW_CLAZZLOG]?.let { newClazzLogJson ->
            //if the previous screen was to schedule a new occurrence, then add this to the list
            // we will give to the view
            safeParse(di, ClazzLog.serializer(), newClazzLogJson)
        }


        val clazzLog = db.onRepoWithFallbackToDb(2000) {
            if(currentClazzLogUid == 0L && newClazzLog != null) {
                newClazzLog
            }else {
                it.takeIf { currentClazzLogUid != 0L }?.clazzLogDao?.findByUidAsync(currentClazzLogUid)
            }
        } ?: ClazzLog()

        val clazzWithSchool = withTimeoutOrNull(2000) {
            db.takeIf { clazzLog.clazzLogClazzUid != 0L }?.clazzDao?.getClazzWithSchool(clazzLog.clazzLogClazzUid)
        } ?: ClazzWithSchool()

        view.clazzLogTimezone = clazzWithSchool.effectiveTimeZone()

        //Find all those who are members of the class at the corresponding class schedule.
        val clazzMembersAtTime = db.clazzEnrolmentDao.getAllClazzEnrolledAtTimeAsync(
                clazzLog.clazzLogClazzUid, clazzLog.logDate, ClazzEnrolment.ROLE_STUDENT)


        var clazzAttendanceLogsInDb = db.clazzLogAttendanceRecordDao.findByClazzLogUid(currentClazzLogUid)
        clazzAttendanceLogsInDb = clazzAttendanceLogsInDb.map { dbAttendanceRec ->
                    clazzAttendanceRecords.firstOrNull { jsonAttendanceRec ->
                    jsonAttendanceRec.clazzLogAttendanceRecordClazzLogUid == clazzLog.clazzLogUid &&
                        dbAttendanceRec.clazzLogAttendanceRecordPersonUid ==
                            jsonAttendanceRec.clazzLogAttendanceRecordPersonUid
                    } ?: dbAttendanceRec
                }

        /* Figure out what to display for class members. In order of preference:
          1) The attendance record as stored in memory/json clazzAttendanceRecords
          2) The attendance record from the database
          3) A new blank attendance record
         */
        val allMembers = clazzMembersAtTime.map { clazzMember ->
            clazzAttendanceRecords.firstOrNull {
                it.clazzLogAttendanceRecordPersonUid == clazzMember.clazzEnrolmentPersonUid &&
                        it.clazzLogAttendanceRecordClazzLogUid == currentClazzLogUid
            } ?: clazzAttendanceLogsInDb.firstOrNull {
                it.clazzLogAttendanceRecordPersonUid == clazzMember.clazzEnrolmentPersonUid &&
                        it.clazzLogAttendanceRecordClazzLogUid == currentClazzLogUid
            } ?:ClazzLogAttendanceRecordWithPerson().apply {
                person = clazzMember.person
                clazzLogAttendanceRecordClazzLogUid = currentClazzLogUid
                clazzLogAttendanceRecordPersonUid = clazzMember.clazzEnrolmentPersonUid
            }
        }.sortedBy { "${it.person?.firstNames} ${it.person?.lastName}" }

        attendanceRecordOneToManyJoinHelper.liveList.postValue(allMembers)

        if(view.clazzLogsList == null) {
            val clazzLogs = repo.clazzLogDao.findByClazzUidAsync(clazzLog.clazzLogClazzUid,
                    ClazzLog.STATUS_HOLIDAY).toMutableList()
            if(newClazzLog != null) {
                clazzLogs.add(newClazzLog)
                clazzLogs.sortBy { it.logDate }
            }

            view.clazzLogsList = clazzLogs.toList()
        }

        return clazzLog
    }

    fun handleClickMarkAll(attendanceStatus: Int) {
        val newList = attendanceRecordOneToManyJoinHelper.liveList.getValue()?.toList()?.map {
            it.copy().also {
                it.attendanceStatus = attendanceStatus
            }
        } ?: return

        attendanceRecordOneToManyJoinHelper.liveList.setValue(newList)
    }

    fun handleSelectClazzLog(current: ClazzLog, next: ClazzLog) {
        presenterScope.launch {
            //save to the database
            updateAttendanceRecordsFromView()
            view.entity = next
            currentClazzLogUid = next.clazzLogUid
            onLoadEntityFromDb(repo)
        }
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzLog? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        var editEntity: ClazzLog? = null
        if(entityJsonStr != null) {
            editEntity = safeParse(di, ClazzLog.serializer(), entityJsonStr)
        }else {
            editEntity = ClazzLog()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, json, ClazzLog.serializer(), entityVal)
    }

    /**
     * Update internal memory and JSON structures
     */
    private fun updateAttendanceRecordsFromView() {
        val clazzLogAttendanceRecords = attendanceRecordOneToManyJoinHelper.liveList.getValue() ?: return
        clazzLogAttendanceRecords.forEach { viewClazzLogAttendanceRec ->
            val existingIndex = this.clazzAttendanceRecords.indexOfFirst {
                it.clazzLogAttendanceRecordClazzLogUid == viewClazzLogAttendanceRec.clazzLogAttendanceRecordClazzLogUid &&
                        it.clazzLogAttendanceRecordPersonUid == viewClazzLogAttendanceRec.clazzLogAttendanceRecordPersonUid
            }

            if(existingIndex >= 0) {
                this.clazzAttendanceRecords[existingIndex] = viewClazzLogAttendanceRec
            }else {
                this.clazzAttendanceRecords.add(viewClazzLogAttendanceRec)
            }
        }

        val viewEntity = view.entity
        if(viewEntity != null && !clazzLogs.any { it.clazzLogUid == viewEntity.clazzLogUid }) {
            //add the current clazzlog to the list of those that need checked for insert/update
            clazzLogs.add(viewEntity)
        }
    }

    private suspend fun commitToDatabase() {
        this.clazzLogs.forEach { entity ->
            val entityClazzAttendanceRecords = clazzAttendanceRecords.filter {
                it.clazzLogAttendanceRecordClazzLogUid == entity.clazzLogUid
            }

            entity.clazzLogStatusFlag = ClazzLog.STATUS_RECORDED
            entity.clazzLogNumPresent = entityClazzAttendanceRecords.count { it.attendanceStatus == STATUS_ATTENDED }
            entity.clazzLogNumAbsent = entityClazzAttendanceRecords.count { it.attendanceStatus == STATUS_ABSENT }
            entity.clazzLogNumPartial = entityClazzAttendanceRecords.count { it.attendanceStatus == STATUS_PARTIAL }
            if(entity.clazzLogUid != 0L) {
                repo.clazzLogDao.updateAsync(entity)
            }else {
                entity.clazzLogUid = repo.clazzLogDao.insertAsync(entity)
                entityClazzAttendanceRecords.forEach {
                    it.clazzLogAttendanceRecordClazzLogUid = entity.clazzLogUid
                }
            }
        }

        val insertUpdatePartition = clazzAttendanceRecords.partition { it.clazzLogAttendanceRecordUid == 0L }
        repo.clazzLogAttendanceRecordDao.insertListAsync(insertUpdatePartition.first)
        repo.clazzLogAttendanceRecordDao.updateListAsync(insertUpdatePartition.second)

        //now update the average attendance for the class
        repo.clazzDao.updateClazzAttendanceAverageAsync(
            clazzLogs.firstOrNull()?.clazzLogClazzUid ?: 0, systemTimeInMillis())
    }

    override fun handleClickSave(entity: ClazzLog) {
        GlobalScope.launch(doorMainDispatcher()) {
            updateAttendanceRecordsFromView()
            commitToDatabase()
            if(arguments[ARG_NEW_CLAZZLOG] != null) {
                systemImpl.popBack(ClazzLogEditView.VIEW_NAME, true, context)
            }else {
                finishWithResult(safeStringify(di,
                    ListSerializer(ClazzLog.serializer()), listOf(entity)))
            }

        }
    }

    companion object {
        const val STATE_CURRENT_UID = "currentUid"
    }

}