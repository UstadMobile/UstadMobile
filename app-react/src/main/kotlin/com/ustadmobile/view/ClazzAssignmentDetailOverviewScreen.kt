package com.ustadmobile.view

import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.SubmissionPolicyConstants
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.ClazzAssignmentDetailOverviewUiState
import com.ustadmobile.core.viewmodel.UstadAssignmentSubmissionHeaderUiState
import com.ustadmobile.core.viewmodel.listItemUiState
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.hooks.useFormattedTime
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import csstype.px
import kotlinx.datetime.TimeZone
import mui.icons.material.*
import mui.material.*
import mui.system.responsive
import mui.material.List
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

val ASSIGNMENT_STATUS_MAP = mapOf(
    CourseAssignmentSubmission.NOT_SUBMITTED to Done.create(),
    CourseAssignmentSubmission.SUBMITTED to Done.create(),
    CourseAssignmentSubmission.MARKED to DoneAll.create()
)

external interface ClazzAssignmentDetailOverviewScreenProps : Props {

    var uiState: ClazzAssignmentDetailOverviewUiState

    var onClickFilterChip: (MessageIdOption2) -> Unit

    var onClickMark: (CourseAssignmentMarkWithPersonMarker) -> Unit

    var onClickComment: (CommentsWithPerson) -> Unit

    var onClickNewPublicComment: () -> Unit

    var onClickNewPrivateComment: () -> Unit

}


private val ClazzAssignmentDetailOverviewScreenComponent2 =
    FC<ClazzAssignmentDetailOverviewScreenProps> { props ->

        val strings = useStringsXml()

        val formattedDateTime = useFormattedDateAndTime(
            timeInMillis = props.uiState.clazzAssignment?.block?.cbDeadlineDate ?: 0,
            timezoneId = TimeZone.currentSystemDefault().id
        )

        val caSubmissionPolicyText = strings[SubmissionPolicyConstants.SUBMISSION_POLICY_MESSAGE_IDS[
                props.uiState.clazzAssignment?.caSubmissionPolicy ?:
                ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE].messageId]


        val caFileType = strings[
                SubmissionConstants.FILE_TYPE_MAP[
                   props.uiState.clazzAssignment?.caFileType ?: ClazzAssignment.FILE_TYPE_DOC
                ] ?: MessageID.message
        ]

        Container {
            maxWidth = "lg"

            Stack {
                spacing = responsive(20.px)

                if (props.uiState.caDescriptionVisible){
                    Typography {
                        + (props.uiState.clazzAssignment?.caDescription ?: "")
                    }
                }

                if (props.uiState.cbDeadlineDateVisible){
                    UstadDetailField {
                        valueText = ReactNode(
                            "$formattedDateTime (${TimeZone.currentSystemDefault().id})"
                        )
                        labelText = strings[MessageID.deadline]
                        icon = EventAvailable.create()
                        onClick = {  }
                    }
                }

                UstadDetailField {
                    valueText = ReactNode(caSubmissionPolicyText)
                    labelText = strings[MessageID.submission_policy]
//                    icon = SUBMISSION_POLICY_MAP[uiState.clazzAssignment?.caSubmissionPolicy]
//                        ?: R.drawable.ic_baseline_task_alt_24,
                    onClick = {  }
                }

                UstadAssignmentSubmissionHeader {
                    uiState = UstadAssignmentSubmissionHeaderUiState(
                        assignmentStatus = CourseAssignmentSubmission.NOT_SUBMITTED
                    )
                }

                Stack {
                    direction = responsive(StackDirection.row)

                    Book {
                        sx {
                            width = 70.px
                            height = 70.px
                        }
                    }
                }

                Typography {
                    + strings[MessageID.grades_class_age]
                }


                UstadListFilterChipsHeader{
                    filterOptions = props.uiState.gradeFilterChips
                    selectedChipId = props.uiState.selectedChipId
                    enabled = props.uiState.fieldsEnabled
                    onClickFilterChip = { onClickFilterChip(it) }
                }

                List{
                    props.uiState.markList.forEach { markItem ->
                        MarkListItem {
                            mark = markItem
                            onClick = props.onClickMark
                        }
                    }
                }

                Typography {
                    + strings[MessageID.class_comments]
                }


                Stack{
                    direction = responsive(StackDirection.row)

                    AccountCircle {
                        sx {
                            width = 40.px
                            height = 40.px
                        }
                    }

                    UstadTextEditField {
                        value = ""
                        label = strings[MessageID.add_class_comment]
                        readOnly = true
                        enabled = props.uiState.fieldsEnabled
                        onChange = {
                            props.onClickNewPublicComment()
                        }
                    }
                }

                List{

                    ListItem {
                        ListItemText {
                            primary = ReactNode(strings[MessageID.class_comments])
                        }
                    }

                    UstadAddCommentListItem {
                        text = strings[MessageID.add_class_comment]
                        enabled = props.uiState.fieldsEnabled
                        personUid = 0
                        onClickSubmit = { props.onClickNewPublicComment() }
                    }

                    props.uiState.publicCommentList.forEach { commentItem ->
                        CommentListItem {
                            comment = commentItem
                            onClick = props.onClickComment
                        }
                    }
                }

                List{

                    ListItem{
                        ListItemText {
                            primary = ReactNode(strings[MessageID.private_comments])
                        }
                    }

                    UstadAddCommentListItem{
                        text = strings[MessageID.add_private_comment]
                        enabled = props.uiState.fieldsEnabled
                        personUid = 0
                        onClickSubmit = { props.onClickNewPrivateComment() }
                    }

                    props.uiState.privateCommentList.forEach { commentItem ->
                        CommentListItem {
                            comment = commentItem
                            onClick = props.onClickComment
                        }
                    }
                }
            }
        }
}


external interface MarkListItemProps : Props {

    var mark: CourseAssignmentMarkWithPersonMarker

    var onClick: (CourseAssignmentMarkWithPersonMarker) -> Unit

}

private val MarkListItem = FC<MarkListItemProps> { props ->

    val strings = useStringsXml()

    val markUiSate = props.mark.listItemUiState
    var text = props.mark.marker?.fullName() ?: ""

    if (markUiSate.markerGroupNameVisible){
        text += "  (${strings[MessageID.group_number]
            .replace("%1\$s", props.mark.camMarkerSubmitterUid.toString())})"
    }

    val formattedTime = useFormattedTime(props.mark.camLct.toInt())

    ListItem{
        ListItemButton {
            onClick = {
                props.onClick(props.mark)
            }

            ListItemIcon {
                + AccountCircle.create()
            }

            ListItemText {
                primary = ReactNode(text)
                secondary = Stack.create {
                    direction = responsive(StackDirection.column)

                    Typography {
                        + ("")
                    }
                }
            }
        }
        secondaryAction = Typography.create {
            + formattedTime
        }
    }
}



external interface CommentListItemProps : Props {

    var comment: CommentsWithPerson

    var onClick: (CommentsWithPerson) -> Unit

}

private val CommentListItem = FC<CommentListItemProps> { props ->

    val formattedTime = useFormattedTime(props.comment.commentsDateTimeAdded.toInt())

    ListItem {
        ListItemButton {
            onClick = { props.onClick }

            ListItemIcon {
                + AccountCircle.create()
            }

            ListItemText {
                primary = ReactNode(props.comment.commentsPerson?.fullName() ?: "")
                secondary = ReactNode(props.comment.commentsText ?: "")
            }
        }

        secondaryAction = Typography.create {
            + formattedTime
        }
    }
}


val ClazzAssignmentDetailOverviewScreenPreview = FC<Props> {

    ClazzAssignmentDetailOverviewScreenComponent2 {
        uiState = ClazzAssignmentDetailOverviewUiState(
            clazzAssignment = ClazzAssignmentWithCourseBlock().apply {
                caDescription = "Read the stories and describe the main characters."
                caSubmissionPolicy = ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE
                block = CourseBlock().apply {
                    cbDeadlineDate = 1677063785
                }
            },
            markList = listOf(
                CourseAssignmentMarkWithPersonMarker().apply {
                    marker = Person().apply {
                        firstNames = "John"
                        lastName = "Smith"
                        isGroup = true
                        camMarkerSubmitterUid = 2
                        camMarkerComment = "Comment"
                    }
                }
            ),
            publicCommentList = listOf(
                CommentsWithPerson().apply {
                    commentsUid = 1
                    commentsPerson = Person().apply {
                        firstNames = "Bob"
                        lastName = "Dylan"
                    }
                    commentsText = "I like this activity. Shall we discuss this in our next meeting?"
                }
            ),
            privateCommentList = listOf(
                CommentsWithPerson().apply {
                    commentsUid = 1
                    commentsPerson = Person().apply {
                        firstNames = "Bob"
                        lastName = "Dylan"
                    }
                    commentsText = "I like this activity. Shall we discuss this in our next meeting?"
                }
            )
        )
    }
}