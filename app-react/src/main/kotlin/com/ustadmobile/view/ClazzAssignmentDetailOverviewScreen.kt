package com.ustadmobile.view

import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.SubmissionPolicyConstants
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.ClazzAssignmentDetailOverviewUiState
import com.ustadmobile.core.viewmodel.UstadAssignmentSubmissionHeaderUiState
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import com.ustadmobile.view.ClazzAssignmentDetailOverviewComponent.Companion.SUBMISSION_POLICY_MAP
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
import com.ustadmobile.core.viewmodel.UstadCourseAssignmentMarkListItem as UstadCourseAssignmentMarkListItemUiState

val ASSIGNMENT_STATUS_MAP = mapOf(
    CourseAssignmentSubmission.NOT_SUBMITTED to Done.create(),
    CourseAssignmentSubmission.SUBMITTED to Done.create(),
    CourseAssignmentSubmission.MARKED to DoneAll.create()
)

external interface ClazzAssignmentDetailOverviewScreenProps : Props {

    var uiState: ClazzAssignmentDetailOverviewUiState

    var onClickFilterChip: (MessageIdOption2) -> Unit

    var onClickMark: (CourseAssignmentMarkWithPersonMarker) -> Unit

    var onClickNewPublicComment: () -> Unit

    var onClickNewPrivateComment: () -> Unit

    var onClickOpenSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit

    var onClickDeleteSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit

    var onClickAddTextSubmission: () -> Unit

    var onClickAddFileSubmission: () -> Unit

    var onClickSubmitSubmission: () -> Unit

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
                    icon = (ASSIGNMENT_STATUS_MAP[props.uiState.clazzAssignment?.caSubmissionPolicy]
                        ?: Done.create())
                    onClick = {  }
                }

                UstadAssignmentSubmissionHeader {
                    uiState = UstadAssignmentSubmissionHeaderUiState(
                        assignmentStatus = CourseAssignmentSubmission.NOT_SUBMITTED
                    )
                }

                Typography {
                    + strings[MessageID.submissions]
                }

                List{
                    props.uiState.draftSubmissionList.forEach { submissionItem ->
                        UstadAssignmentSubmissionListItem {
                            submission = submissionItem
                            onClickOpenSubmission = props.onClickOpenSubmission
                            onClickDeleteSubmission = props.onClickDeleteSubmission
                        }
                    }
                }

                if (props.uiState.addTextVisible) {
                    Button {
                        onClick = { props.onClickAddTextSubmission() }
                        disabled = !props.uiState.fieldsEnabled

                        variant = ButtonVariant.outlined
                        + strings[MessageID.add_text].uppercase()
                    }
                }

                if (props.uiState.addFileVisible) {
                    Button {
                        onClick = { props.onClickAddFileSubmission() }
                        disabled = !props.uiState.fieldsEnabled

                        variant = ButtonVariant.outlined
                        + strings[MessageID.add_file].uppercase()
                    }
                }

                Stack {
                    direction = responsive(StackDirection.row)


                    if (props.uiState.addFileVisible) {
                        Typography{
                            + strings[MessageID.file_type_chosen]
                        }

                        Typography{
                            + caFileType
                        }

                        Box{
                            sx {
                                width = 5.px
                            }
                        }

                        Typography{
                            + strings[MessageID.max_number_of_files]
                                .replace("%1\$s", (props.uiState.clazzAssignment?.caNumberOfFiles ?: 0).toString())

                        }
                    }
                }

                if (props.uiState.unassignedErrorVisible) {
                    Typography{
                        + (props.uiState.unassignedError ?: "")

                    }
                }

                if (props.uiState.submitSubmissionButtonVisible) {
                    Button {
                        onClick = { props.onClickSubmitSubmission() }
                        disabled = !props.uiState.fieldsEnabled
                        variant = ButtonVariant.contained

                        + strings[MessageID.submit].uppercase()
                    }
                }

                List{
                    props.uiState.submittedSubmissionList.forEach { submissionItem ->
                        UstadAssignmentSubmissionListItem{
                            submission = submissionItem
                            onClickOpenSubmission = props.onClickOpenSubmission
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
                    onClickFilterChip = { props.onClickFilterChip(it) }
                }

                List{
                    props.uiState.markList.forEach { markItem ->
                        UstadCourseAssignmentMarkListItem {
                            onClickMark = props.onClickMark
                            uiState = UstadCourseAssignmentMarkListItemUiState(
                                mark = markItem,
                                block = props.uiState.clazzAssignment?.block ?: CourseBlock()
                            )
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

                    props.uiState.publicCommentList.forEach { comment ->
                        UstadCommentListItem {
                            commentWithPerson = comment
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

                    props.uiState.privateCommentList.forEach { comment ->
                        UstadCommentListItem {
                            commentWithPerson = comment
                        }
                    }
                }
            }
        }
    }


val ClazzAssignmentDetailOverviewScreenPreview = FC<Props> {

    val uiStateVal = ClazzAssignmentDetailOverviewUiState(
        addFileVisible = true,
        addTextVisible = true,
        clazzAssignment = ClazzAssignmentWithCourseBlock().apply {
            caDescription = "Read the stories and describe the main characters."
            caSubmissionPolicy = ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE
            caFileType =  ClazzAssignment.FILE_TYPE_DOC
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
        ),
        submissionHeaderUiState = UstadAssignmentSubmissionHeaderUiState(
            assignmentStatus = CourseAssignmentSubmission.MARKED,
            assignmentMark = AverageCourseAssignmentMark().apply {
                averagePenalty = 12
            }
        ),
        submittedSubmissionList = listOf(
            CourseAssignmentSubmissionWithAttachment().apply {
                casUid = 1
                casTimestamp = 1677744388299
                casType = CourseAssignmentSubmission.SUBMISSION_TYPE_FILE
                attachment = CourseAssignmentSubmissionAttachment().apply {
                    casaFileName = "Content Title"
                }
            },
        ),
        draftSubmissionList = listOf(
            CourseAssignmentSubmissionWithAttachment().apply {
                casUid = 1
                casTimestamp = 1677744388299
                casType = CourseAssignmentSubmission.SUBMISSION_TYPE_FILE
                attachment = CourseAssignmentSubmissionAttachment().apply {
                    casaFileName = "Content Title"
                }
            },
        )
    )

    ClazzAssignmentDetailOverviewScreenComponent2 {
        uiState = uiStateVal
    }
}