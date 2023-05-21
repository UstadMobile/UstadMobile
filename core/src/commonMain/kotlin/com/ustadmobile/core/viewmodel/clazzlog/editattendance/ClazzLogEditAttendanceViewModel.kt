package com.ustadmobile.core.viewmodel.clazzlog.editattendance

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.composites.PersonAndClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
}

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

    private var loadClazzLogJob: Job? = null

    init {
        viewModelScope.launch {
            //load the clazzloglist
            val newClazzLogVal = newClazzLog

            loadEntity(
                serializer = ListSerializer(ClazzLog.serializer()),
                onLoadFromDb = { db ->
                    val dbLogList = if(newClazzLogVal != null) {
                        db.clazzLogDao.findByClazzUidAsync(newClazzLogVal.clazzLogUid,
                            ClazzLog.STATUS_RESCHEDULED)
                    }else {
                        db.clazzLogDao.findAllForClazzByClazzLogUid(entityUidArg,
                            ClazzLog.STATUS_RESCHEDULED)
                    }

                    val list = if(newClazzLogVal != null) {
                        (dbLogList + listOf(newClazzLogVal)).sortedByDescending { it.logDate }
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
                    _uiState.value.indexOfClazzLogUid(newClazzLogVal.clazzLogClazzUid) ?: 0
                }

                //EntityUidArg was specified - go to it
                entityUidArg != 0L -> {
                    _uiState.value.indexOfClazzLogUid(entityUidArg) ?: 0
                }

                //no savedstate, no argument?
                else -> 0
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
            }.also {
                savedStateHandle.setJson(
                    savedStateKey,
                    ListSerializer(PersonAndClazzLogAttendanceRecord.serializer()),
                    it
                )
            }

            _uiState.update { prev ->
                prev.copy(
                    clazzLogAttendanceRecordList = personAndAttendanceRecords
                )
            }
        }
    }

    fun onClazzLogAttendanceChanged(record: ClazzLogAttendanceRecordWithPerson) {

    }

    fun onClickMarkAll(status: Int) {

    }


    companion object {

        /**
         * When a new clazzlog is provided as an argument, it will be added to the list of available
         * clazzlogs from which the user can select. This
         */
        const val ARG_NEW_CLAZZLOG = "newclazzlog"

        const val DEST_NAME = "EditAttendance"

        const val STATE_KEY_CURRENT_LOG_INDEX = "activeIndex"

        const val STATE_KEY_CLAZZLOG_LIST = "clazzLogs"

        const val STATE_KEY_LOG_PREFIX = "log_"

    }
}
