package com.ustadmobile.view

import com.ustadmobile.core.controller.SubmissionConstants
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.SubmissionPolicyConstants
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewUiState
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import csstype.JustifyContent
import csstype.px
import kotlinx.datetime.TimeZone
import mui.material.*
import mui.system.responsive
import mui.material.List
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import com.ustadmobile.core.viewmodel.UstadCourseAssignmentMarkListItemUiState as UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.mui.components.UstadCourseAssignmentMarkListItem
import com.ustadmobile.wrappers.quill.ReactQuill
import mui.icons.material.Done
import mui.icons.material.DoneAll
import mui.icons.material.EventAvailable
import mui.icons.material.Add
import mui.icons.material.InsertDriveFile as InsertDriveFileIcon

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
            timeInMillis = props.uiState.courseBlock?.cbDeadlineDate ?: 0,
            timezoneId = TimeZone.currentSystemDefault().id
        )

        val caSubmissionPolicyText = strings[SubmissionPolicyConstants.SUBMISSION_POLICY_MESSAGE_IDS[
                props.uiState.assignment?.caSubmissionPolicy ?:
                ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE].messageId]


        val caFileType = strings[
                SubmissionConstants.FILE_TYPE_MAP[
                        props.uiState.assignment?.caFileType] ?: MessageID.document
        ]

        Container {
            maxWidth = "lg"

            Stack {
                spacing = responsive(20.px)

                UstadRawHtml {
                    html = props.uiState.courseBlock?.cbDescription ?: ""
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
                    icon = (ASSIGNMENT_STATUS_MAP[
                            props.uiState.assignment?.caSubmissionPolicy] ?: Done.create())
                    onClick = {  }
                }

                UstadAssignmentSubmissionHeader {
                    uiState = props.uiState.submissionHeaderUiState
                }

                if(props.uiState.activeUserCanSubmit) {
                    Typography {
                        + strings[MessageID.your_submission]
                    }

                    if (props.uiState.submissionTextFieldVisible) {
                        ReactQuill {
                            id = "assignment_text"
                        }
                    }

                    List {
                        ListItem {
                            ListItemButton {
                                id = "add_file"
                                onClick = {
                                    props.onClickAddFileSubmission()
                                }
                                ListItemIcon {
                                    Add { }
                                }

                                ListItemText {
                                    primary = ReactNode(strings[MessageID.add_file].uppercase())
                                    secondary = ReactNode(
                                        "${strings[MessageID.file_type_chosen]} $caFileType " +
                                            strings[MessageID.max_number_of_files]
                                                .replace("%1\$s",
                                                    (props.uiState.assignment?.caNumberOfFiles ?: 0)
                                                        .toString())
                                    )
                                }
                            }
                        }

                        props.uiState.latestSubmissionAttachments?.forEach { submissionItem ->
                            ListItem {
                                ListItemButton {
                                    ListItemIcon {
                                        InsertDriveFileIcon {  }
                                    }

                                    ListItemText {
                                        primary = ReactNode(submissionItem.casaFileName ?: "")
                                    }
                                }
                            }
                        }
                    }


                    if (props.uiState.unassignedErrorVisible) {
                        Typography {
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
                }

                List{

                    Typography {
                        + strings[MessageID.grades_class_age]
                    }

                    UstadListFilterChipsHeader{
                        filterOptions = props.uiState.gradeFilterChips
                        selectedChipId = props.uiState.selectedChipId
                        enabled = props.uiState.fieldsEnabled
                        onClickFilterChip = { props.onClickFilterChip(it) }
                    }

                    props.uiState.markList.forEach { markItem ->
                        UstadCourseAssignmentMarkListItem {
                            onClickMark = props.onClickMark
                            uiState = UstadCourseAssignmentMarkListItemUiState(
                                mark = markItem,
                                block = props.uiState.courseBlock ?: CourseBlock()
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
        assignment = ClazzAssignment().apply {
            caRequireTextSubmission = true
        },
        courseBlock = CourseBlock().apply {
            cbDeadlineDate = 1685509200000L
            cbDescription = "Complete your assignment or <b>else</b>"
        },
        submitterUid = 42L,
        addFileVisible = true,
        submissionTextFieldVisible = true,
        hasFilesToSubmit = true,
        latestSubmissionAttachments = listOf(
            CourseAssignmentSubmissionAttachment().apply {
                casaUid = 1L
                casaFileName = "File.pdf"
            },
        ),
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
    )

    ClazzAssignmentDetailOverviewScreenComponent2 {
        uiState = uiStateVal
        onClickDeleteSubmission = {}
    }
}