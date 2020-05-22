package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ClazzLogEditAttendanceView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ClazzLog

import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson
import kotlinx.serialization.builtins.list


class ClazzLogEditAttendancePresenter(context: Any,
                          arguments: Map<String, String>, view: ClazzLogEditAttendanceView,
                          lifecycleOwner: DoorLifecycleOwner,
                          systemImpl: UstadMobileSystemImpl,
                          db: UmAppDatabase, repo: UmAppDatabase,
                          activeAccount: DoorLiveData<UmAccount?> = UmAccountManager.activeAccountLiveData)
    : UstadEditPresenter<ClazzLogEditAttendanceView, ClazzLog>(context, arguments, view, lifecycleOwner, systemImpl,
        db, repo, activeAccount) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private val attendanceRecordOneToManyJoinHelper = DefaultOneToManyJoinEditHelper(ClazzLogAttendanceRecord::clazzLogAttendanceRecordUid,
            "state_ClazzLogAttendanceRecord_list", ClazzLogAttendanceRecordWithPerson.serializer().list,
            ClazzLogAttendanceRecordWithPerson.serializer().list, this) { clazzLogAttendanceRecordUid = it }

    //TODO: add code to onCreate to set the liveList on the view, e.g:
    //

    //TODO: add code to the onLoadEntityFromDb to set the list if loading from the database, e.g.
    // var clazzLogAttendanceRecordList = withTimeoutOrNull(2000) {
    //    db.clazzLogAttendanceRecordDao.findAllByFooFk(entity.fk)
    // }
    //
    // attendanceRecordOneToManyJoinHelperFactory.liveList.sendValue(clazzLogAttendanceRecordList)

    //TODO: Add code to handleClickSave to save the result to the database
    // clazzLogAttendanceRecord.commitToDatabase(repo.clazzLogAttendanceRecordDao) {
    //   it.fk = entity.fk
    // }
    //


    /*
     * TODO: Add any required one to many join helpers here - use these templates (type then hit tab)
     * onetomanyhelper: Adds a one to many relationship using OneToManyJoinEditHelper
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //TODO: Set any additional fields (e.g. joinlist) on the view
        view.clazzLogAttendanceRecordList = attendanceRecordOneToManyJoinHelper.liveList
    }


    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzLog? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val clazzLog = withTimeoutOrNull(2000) {
             db.clazzLogDao.findByUid(entityUid)
        } ?: ClazzLog()

        //Find all those who are members of the class at the corresponding class schedule.
        val clazzMembersAtTime = db.clazzMemberDao.getAllClazzMembersAtTime(clazzLog.clazzLogClazzUid,
            clazzLog.logDate)
        val clazzAttendanceLogsInDb = db.clazzLogAttendanceRecordDao.findByClazzLogUid(entityUid)

        val allMembers = clazzAttendanceLogsInDb + clazzMembersAtTime.filter { clazzMember ->
            ! clazzAttendanceLogsInDb.any { it.clazzLogAttendanceRecordClazzMemberUid == clazzMember.clazzMemberUid }
        }.map { ClazzLogAttendanceRecordWithPerson().apply {
            person = it.person
            clazzLogAttendanceRecordClazzLogUid = entityUid
            clazzLogAttendanceRecordClazzMemberUid = it.clazzMemberUid
        } }.sortedBy { "${it.person?.firstNames} ${it.person?.lastName}" }

        attendanceRecordOneToManyJoinHelper.liveList.sendValue(allMembers)

        return clazzLog
    }

    fun handleClickMarkAll(attendanceStatus: Int) {
        val newList = attendanceRecordOneToManyJoinHelper.liveList.getValue()?.map {
            it.copy().apply { it.attendanceStatus = attendanceStatus }
        } ?: return

        attendanceRecordOneToManyJoinHelper.liveList.setVal(newList)
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

    override fun handleClickSave(entity: ClazzLog) {
        GlobalScope.launch(doorMainDispatcher()) {
            entity.clazzLogStatusFlag = ClazzLog.STATUS_RECORDED
            repo.clazzLogDao.update(entity)

            val clazzLogAttendanceRecords = attendanceRecordOneToManyJoinHelper.liveList.getValue() ?: return@launch
            val insertUpdatePartition = clazzLogAttendanceRecords.partition { it.clazzLogAttendanceRecordUid == 0L }
            repo.clazzLogAttendanceRecordDao.insertListAsync(insertUpdatePartition.first)
            repo.clazzLogAttendanceRecordDao.updateListAsync(insertUpdatePartition.second)

            view.finishWithResult(listOf(entity))
        }
    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}