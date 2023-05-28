package com.ustadmobile.view.clazzassignment.submitterdetail

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.mapLookupOrBlank
import com.ustadmobile.core.viewmodel.clazzassignment.ClazzAssignmentViewModelConstants.SUBMISSION_STAUTUS_MESSAGE_ID
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailUiState
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.hooks.useMuiAppState
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
import csstype.Contain
import csstype.Height
import csstype.Overflow
import csstype.pct
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

    val strings: StringsXml = useStringsXml()

    val muiAppState = useMuiAppState()

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
                            )
                        )
                        secondary = ReactNode(strings[MessageID.status])
                    }
                }
            }

            item(key = "averagescore") {
                ListItem.create {
                    ListItemIcon {
                        EmojiEventsIcon { }
                    }
                    ListItemText {
                        primary = ReactNode("${props.uiState.averageScore} ${strings[MessageID.points]}")
                        secondary = ReactNode(strings[MessageID.score])
                    }
                }
            }

            item(key = "submissionheader") {
                UstadDetailHeader.create {
                    header = ReactNode(strings[MessageID.submissions])
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
                    header = ReactNode(strings[MessageID.grades_scoring])
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
                list = props.uiState.marks,
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
                    }
                }
            }

            item(key = "private_comment_header") {
                UstadDetailHeader.create {
                    header = ReactNode(strings[MessageID.private_comments])
                }
            }

            item(key = "new_private_comment") {
                AssignmentCommentTextFieldListItem.create {
                    onChange = props.onChangePrivateComment
                    label = ReactNode(strings[MessageID.add_private_comment])
                    value = props.uiState.newPrivateCommentText
                    activeUserPersonUid = props.uiState.activeUserPersonUid
                    textFieldId = "course_comment_textfield"
                    onClickSubmit = props.onClickSubmitPrivateComment
                }
            }

            items(
                list = props.uiState.privateCommentsList,
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
        privateCommentsList = listOf(
            CommentsAndName(
                comment = Comments().apply {
                    commentsText = "I like this activity. Shall we discuss this in our next meeting?"
                },
                firstNames = "Bob",
                lastName = "Dylan"
            )
        ),
    )

    ClazzAssignmentSubmitterDetailComponent {
        uiState = uiStateVal
    }
}