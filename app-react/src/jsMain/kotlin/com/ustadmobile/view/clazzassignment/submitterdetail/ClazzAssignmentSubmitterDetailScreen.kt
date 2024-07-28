package com.ustadmobile.view.clazzassignment.submitterdetail

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.impl.locale.mapLookup
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.viewmodel.clazzassignment.ClazzAssignmentViewModelConstants.SUBMISSION_STAUTUS_MESSAGE_ID
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailUiState
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailViewModel
import com.ustadmobile.hooks.useDateFormatter
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useEmptyFlow
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useTimeFormatter
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.CourseAssignmentSubmissionFileAndTransferJob
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import com.ustadmobile.view.clazzassignment.SUBMISSION_STATUS_ICON_MAP
import com.ustadmobile.view.components.UstadDetailHeader
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import js.objects.jso
import mui.material.*
import react.FC
import react.Props
import react.ReactNode
import react.create
import mui.icons.material.EmojiEvents as EmojiEventsIcon
import com.ustadmobile.view.clazzassignment.AssignmentCommentTextFieldListItem
import com.ustadmobile.view.clazzassignment.CourseAssignmentSubmissionComponent
import com.ustadmobile.view.clazzassignment.CourseAssignmentSubmissionFileListItem
import com.ustadmobile.view.clazzassignment.UstadCommentListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

external interface ClazzAssignmentSubmitterDetailProps : Props {

    var uiState: ClazzAssignmentSubmitterDetailUiState

    var newPrivateCommentFlow: Flow<String>

    var onClickSubmitGrade: () -> Unit

    var onClickSubmitGradeAndMarkNext: () -> Unit

    var onClickGradeFilterChip: (MessageIdOption2) -> Unit

    var onChangeDraftMark: (CourseAssignmentMark?) -> Unit

    var onChangePrivateComment: (String) -> Unit

    var onClickSubmitPrivateComment: () -> Unit

    var onToggleSubmissionExpandCollapse: (CourseAssignmentSubmission) -> Unit

    var onClickSubmissionFile: (CourseAssignmentSubmissionFileAndTransferJob) -> Unit

    var onDeleteComment: (Comments) -> Unit

}

val ClazzAssignmentSubmitterDetailComponent = FC<ClazzAssignmentSubmitterDetailProps> { props ->

    val strings: StringProvider = useStringProvider()

    val muiAppState = useMuiAppState()

    val refreshCommandFlow = useEmptyFlow<RefreshCommand>()

    val commentsMediatorResult = useDoorRemoteMediator(
        props.uiState.privateCommentsList, refreshCommandFlow
    )

    val commentsInfiniteQueryResult = usePagingSource(
        pagingSourceFactory = commentsMediatorResult.pagingSourceFactory,
        placeholdersEnabled = true,
    )

    val timeFormatterVal = useTimeFormatter()

    val dateFormatterVal = useDateFormatter()

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        id = "VirtualList"

        content = virtualListContent {
            item(key = "header") {
                ListItem.create {
                    ListItemIcon {
                        SUBMISSION_STATUS_ICON_MAP[props.uiState.submissionStatus]?.invoke()
                    }

                    ListItemText {
                        primary = ReactNode(
                            strings.mapLookup(
                                key = props.uiState.submissionStatus,
                                map = SUBMISSION_STAUTUS_MESSAGE_ID
                            ).capitalizeFirstLetter()
                        )
                        secondary = ReactNode(strings[MR.strings.status])
                    }
                }
            }

            if(props.uiState.scoreSummaryVisible) {
                item(key = "averagescore") {
                    ListItem.create {
                        ListItemIcon {
                            EmojiEventsIcon { }
                        }
                        ListItemText {
                            primary = ReactNode("${props.uiState.averageScore} ${strings[MR.strings.points]}")
                            secondary = ReactNode(strings[MR.strings.score])
                        }
                    }
                }
            }

            props.uiState.submissionList.forEachIndexed { index, submissionAndFiles ->
                val isCollapsedVal = submissionAndFiles.submission.casUid in props.uiState.collapsedSubmissions
                item("submission_${submissionAndFiles.submission.casUid}") {
                    CourseAssignmentSubmissionComponent.create {
                        submission = submissionAndFiles.submission
                        submissionNum = props.uiState.submissionList.size - index
                        isCollapsed = isCollapsedVal
                        onToggleExpandCollapse = {
                            props.onToggleSubmissionExpandCollapse(submissionAndFiles.submission)
                        }
                    }
                }

                if(!isCollapsedVal) {
                    items(
                        list = submissionAndFiles.files,
                        key = { "submissionfile_${it.submissionFile?.casaUid}" }
                    ) { fileItem ->
                        CourseAssignmentSubmissionFileListItem.create {
                            file = fileItem
                            onClick = props.onClickSubmissionFile
                        }
                    }
                }
            }

            item(key = "gradesheader") {
                UstadDetailHeader.create {
                    header = ReactNode(strings[MR.strings.grades_scoring])
                }
            }

            if(props.uiState.markListFilterChipsVisible) {
                item(key = "gradefilterchips") {
                    UstadListFilterChipsHeader.create {
                        onClickFilterChip = props.onClickGradeFilterChip
                        filterOptions = props.uiState.markListFilterOptions
                        selectedChipId = props.uiState.markListSelectedChipId
                    }
                }
            }

            items(
                list = props.uiState.visibleMarks,
                key = { "grade_${it.courseAssignmentMark?.camUid}"}
            ){ mark ->
                UstadCourseAssignmentMarkListItem.create {
                    uiState = props.uiState.markListItemUiState(mark)
                    timeFormatter = timeFormatterVal
                    dateFormatter = dateFormatterVal
                }
            }

            props.uiState.draftMark?.also { draftMarkVal ->
                item(key = "draftmark") {
                    CourseAssignmentMarkEdit.create {
                        draftMark = draftMarkVal
                        markFieldsEnabled = props.uiState.markFieldsEnabled
                        maxPoints = props.uiState.block?.courseBlock?.cbMaxPoints ?: 0f
                        scoreError = props.uiState.submitMarkError
                        onChangeDraftMark = props.onChangeDraftMark
                        onClickSubmitGrade = props.onClickSubmitGrade
                        onClickSubmitGradeAndMarkNext = props.onClickSubmitGradeAndMarkNext
                        submitButtonLabelStringResource = props.uiState.submitGradeButtonMessageId
                        submitGradeButtonAndGoNextStringResource = props.uiState.submitGradeButtonAndGoNextMessageId
                    }
                }
            }

            if(props.uiState.newPrivateCommentTextVisible) {
                item(key = "private_comment_header") {
                    UstadDetailHeader.create {
                        header = ReactNode(strings[MR.strings.private_comments])
                    }
                }

                item(key = "new_private_comment") {
                    AssignmentCommentTextFieldListItem.create {
                        onChange = props.onChangePrivateComment
                        label = ReactNode(strings[MR.strings.add_private_comment])
                        value = props.newPrivateCommentFlow
                        activeUserPersonUid = props.uiState.activeUserPersonUid
                        activeUserPersonName = props.uiState.activeUserPersonName
                        activeUserPictureUri = props.uiState.activeUserPictureUri
                        textFieldId = "private_comment_textfield"
                        onClickSubmit = props.onClickSubmitPrivateComment
                    }
                }
            }

            infiniteQueryPagingItems(
                items = commentsInfiniteQueryResult,
                key = { "comment_${it.comment.commentsUid}" }
            ) { comment ->
                UstadCommentListItem.create {
                    commentsAndName = comment
                    timeFormatter = timeFormatterVal
                    dateFormatter = dateFormatterVal
                    dateTimeNow = props.uiState.localDateTimeNow
                    dayOfWeekMap = props.uiState.dayOfWeekStrings
                    showModerateOptions = props.uiState.showModerateOptions
                    onDeleteComment = props.onDeleteComment
                }
            }
        }

        Container {
            List {
                VirtualListOutlet()
            }
        }
    }
}

val ClazzAssignmentSubmitterDetailScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzAssignmentSubmitterDetailViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(
        ClazzAssignmentSubmitterDetailUiState(), Dispatchers.Main.immediate,
    )

    ClazzAssignmentSubmitterDetailComponent {
        uiState = uiStateVal
        newPrivateCommentFlow = viewModel.newPrivateCommentText
        onChangePrivateComment = viewModel::onChangePrivateComment
        onClickSubmitPrivateComment = viewModel::onSubmitPrivateComment
        onChangeDraftMark = viewModel::onChangeDraftMark
        onClickSubmitGrade = viewModel::onClickSubmitMark
        onClickGradeFilterChip = viewModel::onClickGradeFilterChip
        onToggleSubmissionExpandCollapse = viewModel::onToggleSubmissionExpandCollapse
        onClickSubmissionFile = viewModel::onOpenSubmissionFile
        onDeleteComment = viewModel::onDeleteComment
    }
}
