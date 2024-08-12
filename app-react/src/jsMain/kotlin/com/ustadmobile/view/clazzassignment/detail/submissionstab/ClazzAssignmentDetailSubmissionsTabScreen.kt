package com.ustadmobile.view.clazzassignment.detail.submissionstab

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabUiState
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabViewModel
import com.ustadmobile.hooks.courseTerminologyResource
import com.ustadmobile.hooks.useCourseTerminologyEntries
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useTabAndAppBarHeight
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import web.cssom.*
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import mui.material.*
import mui.material.List
import mui.system.responsive
import react.FC
import react.Props
import react.create


external interface ClazzAssignmentDetailSubmissionsTabComponentProps : Props {

    var uiState: ClazzAssignmentDetailSubmissionsTabUiState

    var refreshCommandFlow: Flow<RefreshCommand>?

    var onClickSubmitter: (AssignmentSubmitterSummary) -> Unit

    var onChangeSortOption: (SortOrderOption) -> Unit

}

@Suppress("unused")
val ClazzAssignmentDetailSubmissionsTabPreview = FC<Props> {

    ClazzAssignmentDetailSubmissionsTabComponent {
        uiState = ClazzAssignmentDetailSubmissionsTabUiState(
            progressSummary = AssignmentProgressSummary().apply {
                totalStudents = 10
                submittedStudents = 2
                markedStudents = 3
            },
            assignmentSubmitterList = {
                ListPagingSource(listOf(
                    AssignmentSubmitterSummary().apply {
                        submitterUid = 1
                        name = "Bob Dylan"
                        latestPrivateComment = "Here is private comment"
                        fileSubmissionStatus = CourseAssignmentSubmission.MARKED
                    },
                    AssignmentSubmitterSummary().apply {
                        submitterUid = 2
                        name = "Morris Rogers"
                        latestPrivateComment = "Here is private comment"
                        fileSubmissionStatus = CourseAssignmentSubmission.SUBMITTED
                    }
                ))
            },
        )
    }
}

val ClazzAssignmentDetailSubmissionsTabScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzAssignmentDetailSubmissionsTabViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(ClazzAssignmentDetailSubmissionsTabUiState())

    ClazzAssignmentDetailSubmissionsTabComponent {
        uiState = uiStateVal
        refreshCommandFlow = viewModel.refreshCommandFlow
        onClickSubmitter = viewModel::onClickSubmitter
        onChangeSortOption = viewModel::onChangeSortOption
    }
}


private val ClazzAssignmentDetailSubmissionsTabComponent = FC<ClazzAssignmentDetailSubmissionsTabComponentProps> { props ->

    val tabAndAppBarHeight = useTabAndAppBarHeight()

    val mediatorResult = useDoorRemoteMediator(
        props.uiState.assignmentSubmitterList, props.refreshCommandFlow ?: emptyFlow()
    )

    val infiniteQueryResult = usePagingSource(
        pagingSourceFactory = mediatorResult.pagingSourceFactory,
        placeholdersEnabled = true
    )

    val courseTerminologyEntries = useCourseTerminologyEntries(props.uiState.courseTerminology)

    val strings = useStringProvider()

    VirtualList {
        style = jso {
            height = "calc(100vh - ${tabAndAppBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            item(key = "header") {
                Stack.create {
                    direction = responsive(StackDirection.row)

                    ClazzAssignmentSummaryColumn {
                        total = props.uiState.progressSummary?.totalStudents
                        label = if(props.uiState.progressSummary?.isGroupAssignment == true) {
                            strings[MR.strings.groups]
                        }else {
                            courseTerminologyResource(
                                terminologyEntries = courseTerminologyEntries,
                                stringProvider = strings,
                                stringResource = MR.strings.students,
                            )
                        }
                        showDivider = true
                    }

                    ClazzAssignmentSummaryColumn {
                        total = props.uiState.progressSummary?.submittedStudents
                        label = strings[MR.strings.submitted_cap]
                        showDivider = true
                    }

                    ClazzAssignmentSummaryColumn {
                        total = props.uiState.progressSummary?.markedStudents
                        label = strings[MR.strings.marked_key]
                    }
                }
            }

            item(key = "sortitem") {
                UstadListSortHeader.create {
                    activeSortOrderOption = props.uiState.sortOption
                    enabled = true
                    onClickSort = props.onChangeSortOption
                    sortOptions = props.uiState.sortOptions
                }
            }

            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { it.submitterUid.toString() }
            ) { submitterSummaryItem ->
                SubmitterSummaryListItem.create {
                    submitterSummary = submitterSummaryItem
                    onClick = props.onClickSubmitter
                }
            }
        }

        Container {
            maxWidth = "lg"

            List {
                VirtualListOutlet()
            }
        }
    }

}

