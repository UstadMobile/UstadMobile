package com.ustadmobile.core.viewmodel.coursegroupset.edit

import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.ext.replace
import com.ustadmobile.core.view.CourseGroupSetDetailView
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
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI

data class CourseGroupSetEditUiState(
    val courseGroupSet: CourseGroupSet? = null,
    val membersList: List<CourseGroupMemberAndName> = emptyList(),
    val courseTitleError: String? = null,
    val numOfGroupsError: String? = null,
    val totalGroupError: String? = null,
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
        viewModelScope.launch {
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
                            db.courseGroupMemberDao.findByCourseGroupSetAndClazz(
                                cgsUid = entityUidArg,
                                clazzUid = clazzUidArg,
                                time = systemTimeInMillis()
                            ).map {
                                if(it.cgm == null) {
                                    CourseGroupMemberAndName().apply {
                                        name = it.name
                                        enrolmentIsActive = it.enrolmentIsActive
                                        personUid = it.personUid
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
                            db.takeIf { entityUidArg != 0L }?.courseGroupSetDao?.findByUidAsync(
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
                courseGroupSet = courseGroupSet
            )
        }
    }

    fun onChangeGroupAssignment(personUid: Long, groupNumber: Int) {
        _uiState.update { prev ->
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
    }


    fun onClickSave() {
        val courseGroupSet = _uiState.value.courseGroupSet ?: return

        viewModelScope.launch {
            val membersToSave = _uiState.value.membersList.mapNotNull {
                it.cgm
            }

            activeDb.withDoorTransactionAsync {
                activeDb.courseGroupSetDao.upsertAsync(courseGroupSet)
                activeDb.courseGroupMemberDao.upsertListAsync(membersToSave)
            }

            finishWithResult(
                CourseGroupSetDetailViewModel.DEST_NAME, courseGroupSet.cgsUid, courseGroupSet
            )
        }
    }

    companion object {
        const val DEST_NAME = "CourseGroupsEdit"

        const val KEY_COURSEGROUPMEMBERS = "courseGroupMembers"
    }

}