package com.ustadmobile.core.viewmodel.coursegroupset.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.impl.appstate.ActionBarButtonUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.replace
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.UstadEditViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.detail.CourseGroupSetDetailViewModel
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.CourseGroupMember
import com.ustadmobile.lib.db.entities.CourseGroupMemberAndName
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI

data class CourseGroupSetEditUiState(
    val courseGroupSet: CourseGroupSet? = null,
    val membersList: List<CourseGroupMemberAndName> = emptyList(),
    val courseTitleError: String? = null,
    val numOfGroupsError: String? = null,
    val fieldsEnabled: Boolean = false,
)

class CourseGroupSetEditViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
) : UstadEditViewModel(di, savedStateHandle, DEST_NAME) {

    private val _uiState: MutableStateFlow<CourseGroupSetEditUiState> = MutableStateFlow(CourseGroupSetEditUiState())

    val uiState: Flow<CourseGroupSetEditUiState> = _uiState.asStateFlow()

    private val clazzUidArg = savedStateHandle[UstadView.ARG_CLAZZUID]?.toLong() ?: 0L

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = createEditTitle(
                    newEntityStringResource = MR.strings.add_new_groups,
                    editEntityStringResource = MR.strings.edit_groups
                ),
                hideBottomNavigation = true,
                loadingState = LoadingUiState.INDETERMINATE,
            )
        }

        launchIfHasPermission(
            permissionCheck = {db ->
                db.coursePermissionDao().personHasPermissionWithClazzAsync2(
                    activeUserPersonUid, clazzUidArg, PermissionFlags.COURSE_MANAGE_STUDENT_ENROLMENT,
                )
            }
        ) {
            val courseGroupSetUid = if(entityUidArg != 0L)
                entityUidArg
            else
                activeDb.doorPrimaryKeyManager.nextIdAsync(CourseGroupSet.TABLE_ID)

            awaitAll(
                async {
                    loadEntity(
                        serializer = ListSerializer(CourseGroupMemberAndName.serializer()),
                        loadFromStateKeys = listOf(KEY_COURSEGROUPMEMBERS),
                        onLoadFromDb = { db ->
                            db.courseGroupMemberDao().findByCourseGroupSetAndClazz(
                                cgsUid = entityUidArg,
                                clazzUid = clazzUidArg,
                                time = systemTimeInMillis(),
                                activeFilter = 0,
                                accountPersonUid = activeUserPersonUid,
                            ).map {
                                if(it.cgm == null) {
                                    CourseGroupMemberAndName().apply {
                                        name = it.name
                                        enrolmentIsActive = it.enrolmentIsActive
                                        personUid = it.personUid
                                        pictureUri = it.pictureUri
                                        cgm = CourseGroupMember().apply {
                                            cgmUid = activeDb.doorPrimaryKeyManager.nextIdAsync(CourseGroupMember.TABLE_ID)
                                            cgmSetUid = courseGroupSetUid
                                            cgmPersonUid = it.personUid
                                        }
                                    }
                                }else {
                                    it
                                }
                            }
                        },
                        makeDefault = {
                            //Should never happen
                            emptyList()
                        },
                        uiUpdate = { memberList ->
                            if(memberList != null) {
                                _uiState.update { prev ->
                                    prev.copy(
                                        membersList = memberList
                                    )
                                }
                            }
                        }
                    )
                },
                async {
                    loadEntity(
                        serializer = CourseGroupSet.serializer(),
                        onLoadFromDb = {db ->
                            db.takeIf { entityUidArg != 0L }?.courseGroupSetDao()?.findByUidAsync(
                                uid = entityUidArg
                            )
                        },
                        makeDefault = {
                            CourseGroupSet().apply {
                                cgsUid = courseGroupSetUid
                                cgsClazzUid = clazzUidArg
                            }
                        },
                        uiUpdate = {
                            _uiState.update { prev ->
                                prev.copy(courseGroupSet = it)
                            }
                        }
                    )
                }
            )

            _appUiState.update { prev ->
                prev.copy(
                    actionBarButtonState = ActionBarButtonUiState(
                        visible = true,
                        text = systemImpl.getString(MR.strings.save),
                        enabled = true,
                        onClick = this@CourseGroupSetEditViewModel::onClickSave
                    ),
                    loadingState = LoadingUiState.NOT_LOADING,
                )
            }
            _uiState.update { prev ->
                prev.copy(fieldsEnabled = true)
            }
        }
    }

    fun onEntityChanged(
        courseGroupSet: CourseGroupSet?
    ) {
        _uiState.update { prev ->
            prev.copy(
                courseGroupSet = courseGroupSet,
                courseTitleError = updateErrorMessageOnChange(
                    prevFieldValue = prev.courseGroupSet?.cgsName,
                    currentFieldValue = courseGroupSet?.cgsName,
                    currentErrorMessage = prev.courseTitleError,
                ),
                numOfGroupsError = updateErrorMessageOnChange(
                    prevFieldValue = prev.courseGroupSet?.cgsTotalGroups,
                    currentFieldValue = courseGroupSet?.cgsTotalGroups,
                    currentErrorMessage = prev.numOfGroupsError,
                )
            )
        }

        scheduleEntityCommitToSavedState(
            entity = courseGroupSet,
            serializer = CourseGroupSet.serializer(),
            commitDelay = 200
        )
    }

    fun onChangeGroupAssignment(personUid: Long, groupNumber: Int) {
        val newState = _uiState.updateAndGet { prev ->
            val currentGroupMemberAndName = prev.membersList.first { it.personUid == personUid }
            prev.copy(
                membersList = prev.membersList.replace(
                    element = currentGroupMemberAndName.copy(
                        cgm = currentGroupMemberAndName.cgm?.shallowCopy {
                            cgmGroupNumber = groupNumber
                        }
                    ),
                    replacePredicate = { it.personUid == personUid }
                )
            )
        }

        viewModelScope.launch {
            savedStateHandle.setJson(
                key = KEY_COURSEGROUPMEMBERS,
                serializer = ListSerializer(CourseGroupMemberAndName.serializer()),
                value = newState.membersList
            )
        }
    }

    fun onClickAssignRandomly() {
        val totalGroups = _uiState.value.courseGroupSet?.cgsTotalGroups ?: 1
        val shuffledPersonUidList = _uiState.value.membersList.map { it.personUid }.shuffled()
        val newMemberList = _uiState.value.membersList.map {
            it.copy(
                cgm = it.cgm?.shallowCopy {
                    cgmGroupNumber = (shuffledPersonUidList.indexOf(it.personUid) % totalGroups) + 1
                }
            )
        }

        _uiState.update { prev ->
            prev.copy(
                membersList = newMemberList
            )
        }

        viewModelScope.launch {
            savedStateHandle.setJson(
                key = KEY_COURSEGROUPMEMBERS,
                serializer = ListSerializer(CourseGroupMemberAndName.serializer()),
                value = newMemberList
            )
        }
    }


    fun onClickSave() {
        val courseGroupSet = _uiState.value.courseGroupSet ?: return

        val membersToSave = _uiState.value.membersList.mapNotNull {
            it.cgm
        }

        val hasInvalidAssignments = membersToSave.any { it.cgmGroupNumber > courseGroupSet.cgsTotalGroups }
        if(hasInvalidAssignments){
            snackDispatcher.showSnackBar(Snack(systemImpl.getString(MR.strings.error)))
        }

        if(courseGroupSet.cgsName.isNullOrBlank()) {
            _uiState.update { prev ->
                prev.copy(
                    courseTitleError = systemImpl.getString(MR.strings.field_required_prompt)
                )
            }
        }

        if(courseGroupSet.cgsTotalGroups < 1) {
            _uiState.update { prev ->
                prev.copy(
                    numOfGroupsError = systemImpl.getString(MR.strings.score_greater_than_zero)
                )
            }
        }

        if(hasInvalidAssignments ||
            _uiState.value.numOfGroupsError != null
            || _uiState.value.courseTitleError != null
        ) {
            return
        }


        viewModelScope.launch {
            activeRepoWithFallback.withDoorTransactionAsync {
                activeRepoWithFallback.courseGroupSetDao().upsertAsync(courseGroupSet)
                activeRepoWithFallback.courseGroupMemberDao().upsertListAsync(membersToSave)
            }

            finishWithResult(
                CourseGroupSetDetailViewModel.DEST_NAME, courseGroupSet.cgsUid, courseGroupSet,
                detailViewExtraArgs = mapOf(ARG_CLAZZUID to clazzUidArg.toString()),
            )
        }
    }

    companion object {
        const val DEST_NAME = "CourseGroupsEdit"

        const val KEY_COURSEGROUPMEMBERS = "courseGroupMembers"
    }

}