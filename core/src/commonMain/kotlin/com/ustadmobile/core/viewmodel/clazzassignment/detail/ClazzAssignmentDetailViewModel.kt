package com.ustadmobile.core.viewmodel.clazzassignment.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.impl.appstate.TabItem
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.viewmodel.DetailViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewViewModel
import com.ustadmobile.lib.db.entities.ClazzAssignment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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

    private val clazzUid = savedStateHandle[ARG_CLAZZUID]?.toLong() ?: 0L

    init {
        val assignmentFlow = activeRepoWithFallback.clazzAssignmentDao()
            .findByUidAndClazzUidAsFlow(entityUidArg, clazzUid)
        val permissionFlow = activeRepoWithFallback.coursePermissionDao()
            .personHasPermissionWithClazzPairAsFlow(
                accountPersonUid = accountManager.currentAccount.personUid,
                clazzUid = clazzUid,
                firstPermission = PermissionFlags.COURSE_VIEW,
                secondPermission = PermissionFlags.COURSE_LEARNINGRECORD_VIEW,
            ).distinctUntilChanged()

        uiState = assignmentFlow.combine(permissionFlow) { clazzAssignment, permissionPair ->
            val (hasCourseViewPermission, hasLearnerRecordPermission) = permissionPair
            if(hasCourseViewPermission) {
                val hasSubmissionsTab = clazzAssignment?.caMarkingType == ClazzAssignment.MARKED_BY_PEERS
                        || hasLearnerRecordPermission

                val tabArgs = mapOf(
                    ARG_ENTITY_UID to entityUidArg.toString(),
                    ARG_CLAZZUID to clazzUid.toString()
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
            }else {
                ClazzAssignmentDetailUiState(tabs = emptyList())
            }
        }
    }

    companion object {

        const val DEST_NAME = "CourseAssignment"

    }

}