package com.ustadmobile.view.clazzassignment.detailoverview

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewUiState
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailoverviewSubmissionUiState
import com.ustadmobile.lib.db.entities.ClazzAssignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import mui.material.Stack
import mui.material.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.ext.textLength
import com.ustadmobile.wrappers.quill.ReactQuill
import react.useMemo

external interface CourseAssignmentSubmissionEditProps : Props {

    var stateFlow: Flow<ClazzAssignmentDetailoverviewSubmissionUiState>

    var overviewUiState: ClazzAssignmentDetailOverviewUiState

    var onChangeSubmissionText: (String) -> Unit

}

val CourseAssignmentSubmissionEditComponent = FC<CourseAssignmentSubmissionEditProps> { props ->
    val strings = useStringProvider()

    val uiStateVal by props.stateFlow.collectAsState(
        ClazzAssignmentDetailoverviewSubmissionUiState(), Dispatchers.Main.immediate
    )

    Stack {
        direction = responsive(StackDirection.column)

        ReactQuill {
            id = "assignment_text"
            value = uiStateVal.editableSubmission?.casText ?: ""
            onChange = props.onChangeSubmissionText
        }

        val submissionLength = useMemo(
            uiStateVal.editableSubmission?.casText,
            props.overviewUiState.assignment?.caTextLimitType
        ) {
            uiStateVal.editableSubmission?.textLength(
                limitType = props.overviewUiState.assignment?.caTextLimitType ?: 0
            )
        }

        if(submissionLength != null) {
            val limitTypeMessageId = if(props.overviewUiState.assignment?.caTextLimitType == ClazzAssignment.TEXT_CHAR_LIMIT) {
                MR.strings.characters
            }else {
                MR.strings.words
            }
            + "${strings[limitTypeMessageId]}: $submissionLength / "
            + "${props.overviewUiState.assignment?.caTextLimit} "
        }
    }
}
