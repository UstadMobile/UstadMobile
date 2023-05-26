package com.ustadmobile.view.clazzassignment.submitterdetail

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.mapLookupOrBlank
import com.ustadmobile.core.viewmodel.clazzassignment.ClazzAssignmentViewModelConstants.SUBMISSION_STAUTUS_MESSAGE_ID
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentDetailStudentProgressUiState
import com.ustadmobile.core.viewmodel.clazzassignment.UstadAssignmentSubmissionHeaderUiState
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.lib.db.composites.CommentsAndName
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.ext.onTextChange
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
import com.ustadmobile.util.ext.addOptionalSuffix
import com.ustadmobile.view.clazzassignment.AssignmentCommentTextFieldListItem
import com.ustadmobile.view.clazzassignment.UstadCommentListItem
import csstype.Display
import csstype.JustifyContent
import mui.system.responsive
import mui.system.sx
import react.useRequiredContext

external interface ClazzAssignmentDetailStudentProgressScreenProps : Props {

    var uiState: ClazzAssignmentDetailStudentProgressUiState

    var onClickSubmitGrade: () -> Unit

    var onClickSubmitGradeAndMarkNext: () -> Unit

    var onClickGradeFilterChip: (MessageIdOption2) -> Unit

    var onClickOpenSubmission: (CourseAssignmentSubmission) -> Unit

    var onChangeDraftMark: (CourseAssignmentMark?) -> Unit

    var onChangePrivateComment: (String) -> Unit

    var onClickSubmitPrivateComment: () -> Unit

}

val ClazzAssignmentDetailStudentProgressScreenComponent2 = FC<ClazzAssignmentDetailStudentProgressScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

    val muiAppState = useMuiAppState()

    val markFileSubmissionSubmitGradeAndNextText = if (props.uiState.submissionScore == null)
        MessageID.submit_grade_and_mark_next
    else
        MessageID.update_grade_and_mark_next

    val markFileSubmissionSubmitGradeText = if (props.uiState.submissionScore == null)
        MessageID.submit_grade
    else
        MessageID.update_grade



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

            item(key = "gradefilterchips") {
                UstadListFilterChipsHeader.create {
                    onClickFilterChip = props.onClickGradeFilterChip
                    filterOptions = props.uiState.gradeFilterOptions
                    selectedChipId = props.uiState.selectedChipId
                }
            }

            items(
                list = props.uiState.submittedMarks,
                key = { "grade_${it.camUid}"}
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

val ClazzAssignmentDetailStudentProgressScreenPreview = FC<Props> {

    val uiStateVal = ClazzAssignmentDetailStudentProgressUiState(
        submissionHeaderUiState = UstadAssignmentSubmissionHeaderUiState(
            assignmentStatus = CourseAssignmentSubmission.MARKED,
            assignmentMark = AverageCourseAssignmentMark().apply {
                averagePenalty = 12
            }
        ),
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
        submittedMarks = listOf(
            CourseAssignmentMarkWithPersonMarker().apply {
                marker = Person().apply {
                    firstNames = "John"
                    lastName = "Smith"
                    isGroup = true
                    camMark = 10f
                    camMarkerSubmitterUid = 2
                    camMarkerComment = "Comment"
                    camPenalty = 3f
                }
            }
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

    ClazzAssignmentDetailStudentProgressScreenComponent2 {
        uiState = uiStateVal
    }
}