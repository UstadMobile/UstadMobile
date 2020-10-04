package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ClazzLogEditAttendanceView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ABSENT
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ATTENDED
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_PARTIAL
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import org.kodein.di.DI


class ClazzLogEditAttendancePresenter(context: Any,
                          arguments: Map<String, String>, view: ClazzLogEditAttendanceView,
                          di: DI,
                          lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<ClazzLogEditAttendanceView, ClazzLog>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private val attendanceRecordOneToManyJoinHelper = DefaultOneToManyJoinEditHelper(ClazzLogAttendanceRecord::clazzLogAttendanceRecordUid,
            "state_ClazzLogAttendanceRecord_list", ClazzLogAttendanceRecordWithPerson.serializer().list,
            ClazzLogAttendanceRecordWithPerson.serializer().list, this) { clazzLogAttendanceRecordUid = it }

    private var currentClazzLogUid: Long = 0

    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        currentClazzLogUid = savedState?.get(STATE_CURRENT_UID)?.toLong() ?: arguments[ARG_ENTITY_UID]?.toLong() ?: 0

        //TODO: Set any additional fields (e.g. joinlist) on the view
        view.clazzLogAttendanceRecordList = attendanceRecordOneToManyJoinHelper.liveList
    }


    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzLog? {
        val clazzLog = withTimeoutOrNull(2000) {
             db.takeIf { currentClazzLogUid != 0L }?.clazzLogDao?.findByUidAsync(currentClazzLogUid)
        } ?: ClazzLog()

        val clazzWithSchool = withTimeoutOrNull(2000) {
            db.takeIf { clazzLog.clazzLogClazzUid != 0L }?.clazzDao?.getClazzWithSchool(clazzLog.clazzLogClazzUid)
        } ?: ClazzWithSchool()

        view.clazzLogTimezone = clazzWithSchool.effectiveTimeZone()

        //Find all those who are members of the class at the corresponding class schedule.
        val clazzMembersAtTime = db.clazzMemberDao.getAllClazzMembersAtTimeAsync(clazzLog.clazzLogClazzUid,
            clazzLog.logDate, ClazzMember.ROLE_STUDENT)
        val clazzAttendanceLogsInDb = db.clazzLogAttendanceRecordDao.findByClazzLogUid(currentClazzLogUid)

        val allMembers = clazzAttendanceLogsInDb + clazzMembersAtTime.filter { clazzMember ->
            ! clazzAttendanceLogsInDb.any { it.clazzLogAttendanceRecordClazzMemberUid == clazzMember.clazzMemberUid }
        }.map { ClazzLogAttendanceRecordWithPerson().apply {
            person = it.person
            clazzLogAttendanceRecordClazzLogUid = currentClazzLogUid
            clazzLogAttendanceRecordClazzMemberUid = it.clazzMemberUid
        } }.sortedBy { "${it.person?.firstNames} ${it.person?.lastName}" }

        attendanceRecordOneToManyJoinHelper.liveList.sendValue(allMembers)

        if(view.clazzLogsList == null) {
            view.clazzLogsList = repo.clazzLogDao.findByClazzUidAsLiveData(clazzLog.clazzLogClazzUid,
                ClazzLog.STATUS_HOLIDAY)
        }

        return clazzLog
    }

    fun handleClickMarkAll(attendanceStatus: Int) {
        val newList = attendanceRecordOneToManyJoinHelper.liveList.getValue()?.toList()?.map {
            it.copy().also {
                it.attendanceStatus = attendanceStatus
            }
        } ?: return

        attendanceRecordOneToManyJoinHelper.liveList.setVal(newList)
    }

    fun handleSelectClazzLog(current: ClazzLog, next: ClazzLog) {
        GlobalScope.launch {
            //save to the database
            saveCurrent(current)
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
            editEntity = Json.parse(ClazzLog.serializer(), entityJsonStr)
        }else {
            editEntity = ClazzLog()
        }

        return editEntity
    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null,
                entityVal)
    }

    private suspend fun saveCurrent(entity: ClazzLog) {
        val clazzLogAttendanceRecords = attendanceRecordOneToManyJoinHelper.liveList.getValue() ?: return
        val insertUpdatePartition = clazzLogAttendanceRecords.partition { it.clazzLogAttendanceRecordUid == 0L }
        repo.clazzLogAttendanceRecordDao.insertListAsync(insertUpdatePartition.first)
        repo.clazzLogAttendanceRecordDao.updateListAsync(insertUpdatePartition.second)

        entity.clazzLogStatusFlag = ClazzLog.STATUS_RECORDED
        entity.clazzLogNumPresent = clazzLogAttendanceRecords.count { it.attendanceStatus == STATUS_ATTENDED }
        entity.clazzLogNumAbsent = clazzLogAttendanceRecords.count { it.attendanceStatus == STATUS_ABSENT }
        entity.clazzLogNumPartial = clazzLogAttendanceRecords.count { it.attendanceStatus == STATUS_PARTIAL }
        GlobalScope.launch() {
            repo.clazzLogDao.update(entity)
            //now update the average attendance for the class
            repo.clazzDao.updateClazzAttendanceAverage(entity.clazzLogClazzUid)
        }


    }

    override fun handleClickSave(entity: ClazzLog) {
        GlobalScope.launch(doorMainDispatcher()) {
            saveCurrent(entity)
            view.finishWithResult(listOf(entity))
        }
    }

    companion object {
        const val STATE_CURRENT_UID = "currentUid"
    }

}