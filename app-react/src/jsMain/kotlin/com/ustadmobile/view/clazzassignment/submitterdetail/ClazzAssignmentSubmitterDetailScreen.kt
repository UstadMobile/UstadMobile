package com.ustadmobile.view.clazzassignment.submitterdetail

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.util.ext.mapLookupOrBlank
import com.ustadmobile.core.viewmodel.clazzassignment.ClazzAssignmentViewModelConstants.SUBMISSION_STAUTUS_MESSAGE_ID
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailUiState
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.composites.CourseAssignmentMarkAndMarkerName
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import com.ustadmobile.view.clazzassignment.CourseAssignmentSubmissionListItem
import com.ustadmobile.view.clazzassignment.SUBMISSION_STATUS_ICON_MAP
import com.ustadmobile.view.components.UstadDetailHeader
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import js.core.jso
import mui.material.*
import react.FC
import react.Props
import react.ReactNode
import react.create
import mui.icons.material.EmojiEvents as EmojiEventsIcon
import com.ustadmobile.view.clazzassignment.AssignmentCommentTextFieldListItem
import com.ustadmobile.view.clazzassignment.UstadCommentListItem

external interface ClazzAssignmentSubmitterDetailProps : Props {

    var uiState: ClazzAssignmentSubmitterDetailUiState

    var onClickSubmitGrade: () -> Unit

    var onClickSubmitGradeAndMarkNext: () -> Unit

    var onClickGradeFilterChip: (MessageIdOption2) -> Unit

    var onClickOpenSubmission: (CourseAssignmentSubmission) -> Unit

    var onChangeDraftMark: (CourseAssignmentMark?) -> Unit

    var onChangePrivateComment: (String) -> Unit

    var onClickSubmitPrivateComment: () -> Unit

}

val ClazzAssignmentSubmitterDetailComponent = FC<ClazzAssignmentSubmitterDetailProps> { props ->

    val strings: StringsXml = useStringProvider()

    val muiAppState = useMuiAppState()

    val commentsInfiniteQueryResult = usePagingSource(
        pagingSourceFactory = props.uiState.privateCommentsList,
        placeholdersEnabled = true,
    )

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            item(key = "header") {
                ListItem.create {
                    ListItemIcon {
                        SUBMISSION_STATUS_ICON_MAP[props.uiState.submissionStatus]?.invoke()
                    }

                    ListItemText {
                        primary = ReactNode(
                            strings.mapLookupOrBlank(
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

            item(key = "submissionheader") {
                UstadDetailHeader.create {
                    header = ReactNode(strings[MR.strings.submissions])
                }
            }

            items(
                list = props.uiState.submissionList,
                key = { it.casUid.toString() }
            ) { submissionItem ->
                CourseAssignmentSubmissionListItem.create {
                    submission = submissionItem
                    onClick = {
                        props.onClickOpenSubmission(submissionItem)
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
                }
            }

            props.uiState.draftMark?.also { draftMarkVal ->
                item(key = "draftmark") {
                    CourseAssignmentMarkEdit.create {
                        draftMark = draftMarkVal
                        maxPoints = props.uiState.courseBlock?.cbMaxPoints ?: 0
                        scoreError = props.uiState.submitMarkError
                        onChangeDraftMark = props.onChangeDraftMark
                        onClickSubmitGrade = props.onClickSubmitGrade
                        onClickSubmitGradeAndMarkNext = props.onClickSubmitGradeAndMarkNext
                        submitButtonLabelMessageId = props.uiState.submitGradeButtonMessageId
                        submitGradeButtonAndGoNextMessageId = props.uiState.submitGradeButtonAndGoNextMessageId
                    }
                }
            }

            item(key = "private_comment_header") {
                UstadDetailHeader.create {
                    header = ReactNode(strings[MR.strings.private_comments])
                }
            }

            item(key = "new_private_comment") {
                AssignmentCommentTextFieldListItem.create {
                    onChange = props.onChangePrivateComment
                    label = ReactNode(strings[MR.strings.add_private_comment])
                    value = props.uiState.newPrivateCommentText
                    activeUserPersonUid = props.uiState.activeUserPersonUid
                    textFieldId = "course_comment_textfield"
                    onClickSubmit = props.onClickSubmitPrivateComment
                }
            }

            infiniteQueryPagingItems(
                items = commentsInfiniteQueryResult,
                key = { it.comment.commentsUid.toString() }
            ) { comment ->
                UstadCommentListItem.create {
                    commentsAndName = comment
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

    val uiStateVal by viewModel.uiState.collectAsState(ClazzAssignmentSubmitterDetailUiState())

    ClazzAssignmentSubmitterDetailComponent {
        uiState = uiStateVal
        onChangePrivateComment = viewModel::onChangePrivateComment
        onClickSubmitPrivateComment = viewModel::onSubmitPrivateComment
        onChangeDraftMark = viewModel::onChangeDraftMark
        onClickSubmitGrade = viewModel::onClickSubmitMark
        onClickGradeFilterChip = viewModel::onClickGradeFilterChip
    }
}

val ClazzAssignmentSubmitterDetailScreenPreview = FC<Props> {

    val uiStateVal = ClazzAssignmentSubmitterDetailUiState(
        courseBlock = CourseBlock().apply {
            cbMaxPoints = 50
        },
        draftMark = CourseAssignmentMark().apply {

        },
        submissionList = listOf(
            CourseAssignmentSubmission().apply {
                casUid = 1
                casTimestamp = 1677744388299
                casText = "I can haz cheezburger"
                casType = CourseAssignmentSubmission.SUBMISSION_TYPE_FILE
            },
        ),
        marks = listOf(
            CourseAssignmentMarkAndMarkerName(
                courseAssignmentMark = CourseAssignmentMark().apply {
                    camMarkerSubmitterUid = 2
                    camMarkerComment = "Comment"
                    camMark = 8.1f
                    camPenalty = 0.9f
                    camMaxMark = 10f
                    camLct = systemTimeInMillis()
                },
                markerFirstNames = "John",
                markerLastName = "Smith",
            )
        ),
        privateCommentsList = {
            ListPagingSource(listOf(
                CommentsAndName(
                    comment = Comments().apply {
                        commentsText = "I like this activity. Shall we discuss this in our next meeting?"
                    },
                    firstNames = "Bob",
                    lastName = "Dylan"
                )
            ))
        },
    )

    ClazzAssignmentSubmitterDetailComponent {
        uiState = uiStateVal
    }
}