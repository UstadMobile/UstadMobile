package com.ustadmobile.core.viewmodel

import com.soywiz.klock.DateTime
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.schedule.ClazzLogCreatorManager
import com.ustadmobile.core.schedule.localEndOfDay
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.ext.createNewClazzAndGroups
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.toTermMap
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.lib.util.getDefaultTimeZoneId
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

data class ClazzEditUiState(

    val fieldsEnabled: Boolean = true,

    val entity: ClazzWithHolidayCalendarAndSchoolAndTerminology? = null,

    val clazzStartDateError: String? = null,

    val clazzEndDateError: String? = null,

    val clazzSchedules: List<Schedule> = emptyList(),

    val courseBlockList: List<CourseBlockWithEntity> = emptyList(),

    val timeZone: String = "UTC"

) {

    class CourseBlockUiState internal constructor(
        val courseBlock: CourseBlockWithEntity
    ) {
        val showIndent: Boolean
            get() = courseBlock.cbType != CourseBlock.BLOCK_MODULE_TYPE && courseBlock.cbIndentLevel < BLOCK_MAX_INDENT

        val showUnindent: Boolean
            get() = courseBlock.cbIndentLevel > 0

        val showHide: Boolean
            get() = !courseBlock.cbHidden

        val showUnhide: Boolean
            get() = courseBlock.cbHidden
    }


    val clazzEditAttendanceChecked: Boolean
        get() = entity?.clazzFeatures == Clazz.CLAZZ_FEATURE_ATTENDANCE
                && Clazz.CLAZZ_FEATURE_ATTENDANCE == Clazz.CLAZZ_FEATURE_ATTENDANCE

    fun courseBlockStateFor(couresBlockWithEntity: CourseBlockWithEntity): CourseBlockUiState {
        return CourseBlockUiState(couresBlockWithEntity)
    }

    companion object {

        const val BLOCK_MAX_INDENT = 3

    }

}

class ClazzEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): UstadEditViewModel(di, savedStateHandle) {

    val _uiState = MutableStateFlow(ClazzEditUiState())

    val uiState: Flow<ClazzEditUiState> = _uiState.asStateFlow()

    init {
        val title = createEditTitle(MessageID.add_a_new_course, MessageID.edit_course)
        _appUiState.update {
            AppUiState(
                title = title,
                actionBarButtonState = ActionBarButtonUiState(
                    visible = true,
                    text = systemImpl.getString(MessageID.save),
                    onClick = this::onClickSave
                ),
                loadingState = LoadingUiState.INDETERMINATE
            )
        }

        _uiState.update { prev -> prev.copy(fieldsEnabled = false) }

        viewModelScope.launch {
            awaitAll(
                async {
                    loadEntity(
                        onLoadFromDb = {
                            it.clazzDao.findByUidWithHolidayCalendarAsync(entityUidArg)
                        },
                        makeDefault = {
                            ClazzWithHolidayCalendarAndSchoolAndTerminology().apply {
                                clazzUid = activeDb.doorPrimaryKeyManager.nextId(Clazz.TABLE_ID)
                                clazzName = ""
                                isClazzActive = true
                                clazzStartTime = systemTimeInMillis()
                                clazzTimeZone = getDefaultTimeZoneId()
                                clazzSchoolUid = savedStateHandle[UstadView.ARG_SCHOOL_UID]?.toLong() ?: 0L
                                school = activeRepo.schoolDao.takeIf { clazzSchoolUid != 0L }
                                    ?.findByUidAsync(clazzSchoolUid)
                                terminology = activeRepo.courseTerminologyDao
                                    .takeIf { clazzTerminologyUid != 0L }
                                    ?.findByUidAsync(clazzTerminologyUid)
                            }
                        },
                        uiUpdate = {
                            _uiState.update { prev ->
                                prev.copy(
                                    entity = it
                                )
                            }
                        }
                    )
                }
            )

            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }
        }

    }

    fun onEntityChanged(entity: ClazzWithHolidayCalendarAndSchoolAndTerminology?) {
        _uiState.update { prev ->
            prev.copy(
                entity = entity
            )
        }

        scheduleEntityCommitToSavedState(
            entity = entity,
            serializer = ClazzWithHolidayCalendarAndSchoolAndTerminology.serializer(),
        )
    }

    private fun ClazzEditUiState.hasErrors() : Boolean {
        return clazzStartDateError != null || clazzEndDateError != null
    }

    fun onClickSave() {
        val initEntity = _uiState.value.entity ?: return

        if (initEntity.clazzStartTime == 0L) {
            _uiState.update { prev ->
                prev.copy(
                    clazzStartDateError = systemImpl.getString(MessageID.field_required_prompt)
                )
            }
        }

        if(initEntity.clazzEndTime <= initEntity.clazzStartTime) {
            _uiState.update { prev ->
                prev.copy(clazzEndDateError = systemImpl.getString(MessageID.error_start_date_before_clazz_date))
            }
        }

        if(_uiState.value.hasErrors())
            return

        loadingState = LoadingUiState.INDETERMINATE

        //Entity to save
        val entity = initEntity.shallowCopy {
            this.clazzName = clazzName?.trim()
            clazzStartTime = DateTime(initEntity.clazzStartTime)
                .toOffsetByTimezone(initEntity.effectiveTimeZone).localMidnight.utc.unixMillisLong

            if(clazzEndTime != Long.MAX_VALUE){
                clazzEndTime = DateTime(initEntity.clazzEndTime)
                    .toOffsetByTimezone(initEntity.effectiveTimeZone).localEndOfDay.utc.unixMillisLong
            }
        }

        _uiState.update { prev -> prev.copy(fieldsEnabled = false, entity = entity) }

        viewModelScope.launch {
            activeDb.withDoorTransactionAsync {
                if(entityUidArg == 0L) {
                    val termMap = activeDb.courseTerminologyDao.findByUidAsync(initEntity.clazzTerminologyUid)
                        .toTermMap(json, systemImpl)
                    activeDb.createNewClazzAndGroups(initEntity, systemImpl, termMap)
                }else {
                    activeDb.clazzDao.updateAsync(initEntity)
                }
            }

            val fromDateTime = DateTime.now().toOffsetByTimezone(entity.effectiveTimeZone).localMidnight
            val clazzLogCreatorManager: ClazzLogCreatorManager by di.instance()
            clazzLogCreatorManager.requestClazzLogCreation(entity.clazzUid,
                accountManager.activeAccount.endpointUrl,
                fromDateTime.utc.unixMillisLong, fromDateTime.localEndOfDay.utc.unixMillisLong)
            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }
            loadingState = LoadingUiState.NOT_LOADING

            finishWithResult(ClazzDetailView.VIEW_NAME, entity.clazzUid, entity)
        }
    }

}
