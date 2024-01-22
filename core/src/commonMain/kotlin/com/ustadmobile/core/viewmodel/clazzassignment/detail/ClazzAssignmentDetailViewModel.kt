package com.ustadmobile.core.viewmodel.clazzassignment.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.TabItem
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewViewModel
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.kodein.di.DI

data class ClazzAssignmentDetailUiState(
    val tabs: List<TabItem> = emptyList(),
)

/**
 * This is ALWAYS used within the assignment detail tab, so the destination name is actually
 * assignmentdetail
 */
class ClazzAssignmentDetailViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle
): DetailViewModel<ClazzAssignment>(di, savedStateHandle, DEST_NAME) {

    val uiState: Flow<ClazzAssignmentDetailUiState>

    init {
        val assignmentFlow = activeRepo.clazzAssignmentDao.findByUidAsFlow(entityUidArg)
        val permissionFlow = activeRepo.clazzAssignmentDao
            .personHasPermissionWithClazzByAssignmentUidAsFlow(
                accountPersonUid = accountManager.currentAccount.personUid,
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
                TabItem(ClazzAssignmentDetailOverviewViewModel.DEST_NAME, tabArgs,
                    systemImpl.getString(MR.strings.clazz_assignment))
            )
            if(hasSubmissionsTab) {
                val tabName = if(!hasLearnerRecordPermission &&
                    clazzAssignment?.caMarkingType == ClazzAssignment.MARKED_BY_PEERS
                ) {
                    systemImpl.getString(MR.strings.peers_to_review)
                }else {
                    systemImpl.getString(MR.strings.submissions)
                }
                tabs.add(
                    TabItem(
                        viewName = ClazzAssignmentDetailSubmissionsTabViewModel.DEST_NAME,
                        args = tabArgs,
                        label = tabName
                    )
                )
            }

            ClazzAssignmentDetailUiState(tabs = tabs.toList())
        }
    }

    companion object {

        const val DEST_NAME = "CourseAssignment"

    }

}