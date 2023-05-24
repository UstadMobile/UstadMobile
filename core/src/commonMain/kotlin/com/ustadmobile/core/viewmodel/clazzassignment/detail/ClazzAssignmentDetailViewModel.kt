package com.ustadmobile.core.viewmodel.clazzassignment.detail

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.appstate.TabItem
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.kodein.di.DI

data class ClazzAssignmentDetailUiState(
    val tabs: List<TabItem> = emptyList(),
)

class ClazzAssignmentDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): DetailViewModel<ClazzAssignment>(di, savedStateHandle, ClazzAssignmentDetailView.VIEW_NAME) {

    val uiState: Flow<ClazzAssignmentDetailUiState>

    init {
        val assignmentFlow = activeRepo.clazzAssignmentDao.findByUidAsFlow(entityUidArg)
        val permissionFlow = activeRepo.clazzAssignmentDao
            .personHasPermissionWithClazzByAssignmentUidAsFlow(
                accountPersonUid = accountManager.activeAccount.personUid,
                clazzAssignmentUid = entityUidArg,
                permission = Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT
        )

        uiState = assignmentFlow.combine(permissionFlow) { clazzAssignment, hasLearnerRecordPermission ->
            val hasSubmissionsTab = clazzAssignment?.caMarkingType == ClazzAssignment.MARKED_BY_PEERS
                || hasLearnerRecordPermission

            val tabArgs = mapOf(
                ARG_ENTITY_UID to entityUidArg.toString(),
                ARG_CLAZZUID to (clazzAssignment?.caClazzUid ?: 0).toString()
            )
            val tabs = mutableListOf(
                TabItem(ClazzAssignmentDetailOverviewView.VIEW_NAME, tabArgs,
                    systemImpl.getString(MessageID.clazz_assignment))
            )
            if(hasSubmissionsTab) {
                tabs.add(TabItem(ClazzAssignmentDetailStudentProgressOverviewListView.VIEW_NAME,
                    tabArgs, systemImpl.getString(MessageID.submissions)))
            }

            ClazzAssignmentDetailUiState(tabs = tabs.toList())
        }
    }

}