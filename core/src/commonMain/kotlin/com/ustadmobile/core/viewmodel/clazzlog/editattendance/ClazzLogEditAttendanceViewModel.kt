package com.ustadmobile.core.viewmodel.clazzlog.editattendance

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.replace
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.clazzlog.edit.ClazzLogEditViewModel
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.composites.PersonAndClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI

data class ClazzLogEditAttendanceUiState(

    val clazzLogAttendanceRecordList: List<PersonAndClazzLogAttendanceRecord> = emptyList(),

    val clazzLogTimezone: String = "UTC",

    val currentClazzLogIndex: Int = 0,

    val clazzLogsList: List<ClazzLog> = emptyList(),

    val fieldsEnabled: Boolean = true,

    val timeZone: String = "UTC"

) {
    internal fun indexOfClazzLogUid(clazzLogUid: Long): Int? {
        return clazzLogsList.indexOfFirst { it.clazzLogUid == clazzLogUid }.let {
            if(it >= 0)
                it
            else
                null
        }
    }

    val currentClazzLog: ClazzLog
        get() = clazzLogsList[currentClazzLogIndex]
}

/**
 * This screen is where a teacher would record attendance. It shows a list of students in the course
 * and allows the teacher to mark each one as present, absent, or partial attendance (e.g. late).
 */
class ClazzLogEditAttendanceViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadEditViewModel(
    di, savedStateHandle, DEST_NAME
) {

    private val _uiState = MutableStateFlow(ClazzLogEditAttendanceUiState())

    val uiState: Flow<ClazzLogEditAttendanceUiState> = _uiState.asStateFlow()

    private val newClazzLog: ClazzLog? by lazy {
        savedStateHandle[ARG_NEW_CLAZZLOG]?.let {
            json.decodeFromString(ClazzLog.serializer(), it)
        }
    }

    private var currentClazzLogIndex: Int?
        get() = savedStateHandle[STATE_KEY_CURRENT_LOG_INDEX]?.toInt()
        set(value) {
            savedStateHandle[STATE_KEY_CURRENT_LOG_INDEX] = value.toString()
        }

    private val ClazzLog.savedStateKey: String
        get() = "$STATE_KEY_LOG_PREFIX${clazzLogUid}"

    private var loadClazzLogJob: Job? = null

    private val saveAttendanceRecordsMutex = Mutex()

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = systemImpl.getString(MessageID.record_attendance),
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.save),
                    onClick = this::onClickSave
                )
            )
        }

        viewModelScope.launch {
            //load the clazzloglist
            val newClazzLogVal = newClazzLog

            loadEntity(
                serializer = ListSerializer(ClazzLog.serializer()),
                onLoadFromDb = { db ->
                    val dbLogList = if(newClazzLogVal != null) {
                        db.clazzLogDao.findByClazzUidAsync(newClazzLogVal.clazzLogClazzUid,
                            ClazzLog.STATUS_RESCHEDULED)
                    }else {
                        db.clazzLogDao.findAllForClazzByClazzLogUid(entityUidArg,
                            ClazzLog.STATUS_RESCHEDULED)
                    }

                    val list = if(newClazzLogVal != null) {
                        (dbLogList + listOf(newClazzLogVal)).sortedBy { it.logDate }
                    }else {
                        dbLogList
                    }

                    list
                },
                makeDefault = {
                    //This should never happen - loadFromDb will always get the list
                    emptyList()
                },
                uiUpdate = {
                    _uiState.update { prev ->
                        prev.copy(clazzLogsList = it ?: emptyList())
                    }
                }
            )

            //now determine the currently active clazzLog to show user
            val currentClazzLogIndexVal = currentClazzLogIndex

            val startIndex: Int = when {
                //There is already something in savedState
                currentClazzLogIndexVal != null -> {
                    currentClazzLogIndexVal
                }

                //New clazzlog was created - start there
                newClazzLogVal != null -> {
                    _uiState.value.indexOfClazzLogUid(newClazzLogVal.clazzLogUid) ?: 0
                }

                //EntityUidArg was specified - go to it
                entityUidArg != 0L -> {
                    _uiState.value.indexOfClazzLogUid(entityUidArg) ?: 0
                }

                //no savedstate, no argument?
                else -> _uiState.value.clazzLogsList.size - 1
            }

            onChangeClazzLog(
                clazzLog = _uiState.value.clazzLogsList[startIndex],
                forceLoad = true
            )
        }
    }

    fun onChangeClazzLog(
        clazzLog: ClazzLog,
        forceLoad: Boolean = false
    ) {
        val logIndex = _uiState.value.clazzLogsList.indexOfFirst {
            it.clazzLogUid == clazzLog.clazzLogUid
        }

        //Do nothing if the viewpager is just settling on the current page
        if(!forceLoad && logIndex == _uiState.value.currentClazzLogIndex)
            return

        _uiState.update { prev ->
            currentClazzLogIndex = logIndex
            prev.copy(
                currentClazzLogIndex = prev.clazzLogsList.indexOfFirst {
                    it.clazzLogUid == clazzLog.clazzLogUid
                }
            )
        }

        loadClazzLogJob?.cancel()
        loadClazzLogJob = viewModelScope.launch {
            val savedStateKey = "$STATE_KEY_LOG_PREFIX${clazzLog.clazzLogUid}"

            //Try to load the list of PersonAndClazzLogAttendanceRecord from SavedState
            val personAndAttendanceRecords = savedStateHandle.getJson(
                key = savedStateKey,
                deserializer = ListSerializer(PersonAndClazzLogAttendanceRecord.serializer())
            )
            //If not in SavedState, then load from database, and put into savedStateHandle
            ?: activeRepo.clazzLogAttendanceRecordDao.findByClazzAndTime(
                clazzLog.clazzLogClazzUid, clazzLog.clazzLogUid, clazzLog.logDate
            ).map {
                if(it.attendanceRecord == null) {
                    PersonAndClazzLogAttendanceRecord(
                        person = it.person,
                        attendanceRecord = ClazzLogAttendanceRecord().apply {
                            clazzLogAttendanceRecordUid = activeDb.doorPrimaryKeyManager
                                .nextIdAsync(ClazzLogAttendanceRecord.TABLE_ID)
                            clazzLogAttendanceRecordPersonUid = it.person?.personUid ?: 0
                            clazzLogAttendanceRecordClazzLogUid = clazzLog.clazzLogUid
                        }
                    )
                }else {
                    it
                }
            }.also { personAndAttendanceRecordsList ->
                savedStateHandle.setJson(
                    key = savedStateKey,
                    serializer = ListSerializer(PersonAndClazzLogAttendanceRecord.serializer()),
                    value = personAndAttendanceRecordsList
                )
            }

            _uiState.update { prev ->
                prev.copy(
                    clazzLogAttendanceRecordList = personAndAttendanceRecords
                )
            }
        }
    }

    fun onClazzLogAttendanceChanged(record: PersonAndClazzLogAttendanceRecord) {
        _uiState.update { prev ->
            prev.copy(
                clazzLogAttendanceRecordList = prev.clazzLogAttendanceRecordList.replace(record) {
                    it.person?.personUid == record.person?.personUid
                }
            )
        }

        viewModelScope.launch {
            commitAttendanceRecordsToState()
        }
    }

    fun onClickMarkAll(status: Int) {
        _uiState.update { prev ->
            prev.copy(
                clazzLogAttendanceRecordList = prev.clazzLogAttendanceRecordList.map { record ->
                    record.copy(
                        attendanceRecord = record.attendanceRecord?.shallowCopy {
                            attendanceStatus = status
                        }
                    )
                }
            )
        }

        viewModelScope.launch {
            commitAttendanceRecordsToState()
        }
    }

    private suspend fun commitAttendanceRecordsToState() {
        saveAttendanceRecordsMutex.withLock {
            savedStateHandle.setJson(
                _uiState.value.currentClazzLog.savedStateKey,
                ListSerializer(PersonAndClazzLogAttendanceRecord.serializer()),
                _uiState.value.clazzLogAttendanceRecordList
            )
        }
    }

    fun onClickSave() {
        viewModelScope.launch {
            //If an automated test is going on, allow the chagnes to be committed
            delay(100)
            saveAttendanceRecordsMutex.withLock {
                val clazzLogsToSave = mutableListOf<ClazzLog>()
                val attendanceRecordsToSave = mutableListOf<ClazzLogAttendanceRecord>()
                savedStateHandle.keys.filter {
                    it.startsWith(STATE_KEY_LOG_PREFIX)
                }.forEach { stateKey ->
                    val clazzLogUid = stateKey.substringAfter(STATE_KEY_LOG_PREFIX).toLong()
                    val clazzLog = _uiState.value.clazzLogsList.first {
                        it.clazzLogUid == clazzLogUid
                    }

                    val logRecords = savedStateHandle.getJson(
                        stateKey,
                        ListSerializer(PersonAndClazzLogAttendanceRecord.serializer()),
                    )?.mapNotNull {
                        it.attendanceRecord
                    } ?: emptyList()
                    clazzLogsToSave += clazzLog.shallowCopy {
                        clazzLogNumPresent = logRecords.count {
                            it.attendanceStatus == ClazzLogAttendanceRecord.STATUS_ATTENDED
                        }
                        clazzLogNumAbsent = logRecords.count {
                            it.attendanceStatus == ClazzLogAttendanceRecord.STATUS_ABSENT
                        }
                        clazzLogNumPartial = logRecords.count {
                            it.attendanceStatus == ClazzLogAttendanceRecord.STATUS_PARTIAL
                        }
                    }

                    attendanceRecordsToSave += logRecords
                }

                activeDb.withDoorTransactionAsync {
                    activeDb.clazzLogDao.upsertListAsync(clazzLogsToSave)
                    activeDb.clazzLogAttendanceRecordDao.upsertListAsync(attendanceRecordsToSave)
                }
            }

            if(newClazzLog != null) {
                //User came via adding new occurence from attendancelist
                navController.popBackStack(viewName = ClazzLogEditViewModel.DEST_NAME, inclusive = true)
            }else {
                finishWithResult(_uiState.value.currentClazzLog)
            }
        }
    }


    companion object {

        /**
         * When a new clazzlog is provided as an argument, it will be added to the list of available
         * clazzlogs from which the user can select. This
         */
        const val ARG_NEW_CLAZZLOG = "newclazzlog"

        const val DEST_NAME = "EditAttendance"

        const val STATE_KEY_CURRENT_LOG_INDEX = "activeIndex"

        const val STATE_KEY_LOG_PREFIX = "log_"

    }
}
