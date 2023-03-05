package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.capitalizeFirstLetter
import com.ustadmobile.core.viewmodel.ClazzAssignmentDetailStudentProgressUiState
import com.ustadmobile.core.viewmodel.UstadAssignmentSubmissionHeaderUiState
import com.ustadmobile.core.viewmodel.UstadCourseAssignmentMarkListItemUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.*
import csstype.px
import mui.material.*
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode


external interface ClazzAssignmentDetailStudentProgressScreenProps : Props {

    var uiState: ClazzAssignmentDetailStudentProgressUiState

    var onClickSubmitGrade: () -> Unit

    var onClickSubmitGradeAndMarkNext: () -> Unit

    var onAddComment: (String) -> Unit

    var onAddMark: (String) -> Unit

    var onClickNewPrivateComment: (String) -> Unit

    var onClickGradeFilterChip: (MessageIdOption2) -> Unit

    var onClickOpenSubmission: (CourseAssignmentSubmissionWithAttachment) -> Unit

}

val ClazzAssignmentDetailStudentProgressScreenComponent2 =
    FC<ClazzAssignmentDetailStudentProgressScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

        val markFileSubmissionSubmitGradeAndNextText = if (props.uiState.submissionScore == null)
            MessageID.submit_grade_and_mark_next
        else
            MessageID.update_grade_and_mark_next

        val markFileSubmissionSubmitGradeText = if (props.uiState.submissionScore == null)
            MessageID.submit_grade
        else
            MessageID.update_grade

    Container {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(20.px)

            Typography {
                + strings[MessageID.submissions]
            }

            UstadAssignmentSubmissionHeader{
                uiState = props.uiState.submissionHeaderUiState
            }

            List{
                props.uiState.submissionList.forEach { submissionItem ->
                    UstadAssignmentSubmissionListItem {
                        submission = submissionItem
                        onClickOpenSubmission = props.onClickOpenSubmission
                    }
                }
            }

            Typography {
                + strings[MessageID.grades_class_age]
            }

            UstadListFilterChipsHeader {
                filterOptions = props.uiState.gradeFilterOptions
                selectedChipId = props.uiState.selectedChipId
                enabled = props.uiState.fieldsEnabled
                onClickFilterChip = props.onClickGradeFilterChip
            }

            List{
                props.uiState.markList.forEach { mark ->
                    UstadCourseAssignmentMarkListItem {
                        uiState = UstadCourseAssignmentMarkListItemUiState(
                            mark = mark,
                            block = props.uiState.assignment?.block ?: CourseBlock()
                        )
                    }
                }
            }


            if (props.uiState.markStudentVisible){
                UstadTextEditField {
                    value = ""
                    label = strings[MessageID.comment]
                    enabled = props.uiState.fieldsEnabled
                    onChange = { comment ->
                        props.onAddComment(comment)
                    }
                }
            }


            if (props.uiState.markStudentVisible){
                UstadTextEditField {
                    value = ""
                    label = (strings[MessageID.points].capitalizeFirstLetter()
                            + props.uiState.assignment?.block?.cbMaxPoints)
                    error = props.uiState.submitMarkError
                    enabled = props.uiState.fieldsEnabled
//                    inputProps = InputType.number
                    onChange = { mark ->
                        props.onAddMark(mark)
                    }
                }
            }

            if (props.uiState.markStudentVisible){
                Button {
                    onClick = { props.onClickSubmitGrade() }
                    variant = ButtonVariant.contained
                    disabled = !props.uiState.markNextStudentVisible

                    + strings[markFileSubmissionSubmitGradeText].uppercase()
                }
            }

            if (props.uiState.markNextStudentVisible){
                Button {
                    onClick = { props.onClickSubmitGradeAndMarkNext() }
                    variant = ButtonVariant.outlined
                    disabled = !props.uiState.markNextStudentVisible

                    + strings[markFileSubmissionSubmitGradeAndNextText].uppercase()
                }
            }

            UstadCourseAssignmentMarkListItem{
                uiState = props.uiState.markListItemUiState
            }


            List{

                ListItem {
                    ListItemText {
                        primary = ReactNode(strings[MessageID.private_comments])
                    }
                }

                UstadAddCommentListItem {
                    text = strings[MessageID.add_private_comment]
                    enabled = props.uiState.fieldsEnabled
                    personUid = 0
                    onClickSubmit = { props.onClickNewPrivateComment }
                }

                props.uiState.privateCommentsList.forEach { comment ->
                    UstadCommentListItem{
                        commentWithPerson = comment
                    }
                }
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
        submissionList = listOf(
            CourseAssignmentSubmissionWithAttachment().apply {
                casUid = 1
                casTimestamp = 1677744388299
                casType = CourseAssignmentSubmission.SUBMISSION_TYPE_FILE
                attachment = CourseAssignmentSubmissionAttachment().apply {
                    casaFileName = "Content Title"
                }
            },
        ),
        markListItemUiState = UstadCourseAssignmentMarkListItemUiState(
            mark = CourseAssignmentMarkWithPersonMarker().apply {
                marker = Person().apply {
                    firstNames = "John"
                    lastName = "Smith"
                    isGroup = true
                    camMarkerSubmitterUid = 2
                    camMarkerComment = "Comment"
                    camPenalty = 3
                }
            }
        ),
        privateCommentsList = listOf(
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

    ClazzAssignmentDetailStudentProgressScreenComponent2 {
        uiState = uiStateVal
    }
}