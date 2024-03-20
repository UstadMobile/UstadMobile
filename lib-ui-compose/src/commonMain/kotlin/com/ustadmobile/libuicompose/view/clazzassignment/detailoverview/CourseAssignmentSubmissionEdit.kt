package com.ustadmobile.libuicompose.view.clazzassignment.detailoverview

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailoverviewSubmissionUiState
import com.ustadmobile.libuicompose.components.UstadRichTextEdit
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

@Composable
fun CourseAssignmentSubmissionEdit(
    stateFlow: Flow<ClazzAssignmentDetailoverviewSubmissionUiState>,
    onChangeSubmissionText: (String) -> Unit,
    onClickEditSubmission: () -> Unit,
) {
    val stateVal by stateFlow.collectAsState(
        ClazzAssignmentDetailoverviewSubmissionUiState(), Dispatchers.Main.immediate,
    )

    UstadRichTextEdit(
        modifier = Modifier
            .testTag("submission_text_field")
            .defaultItemPadding()
            .fillMaxWidth(),
        html = stateVal.editableSubmission?.casText ?: "",
        editInNewScreenLabel = stringResource(MR.strings.text),
        placeholderText = stringResource(MR.strings.text),
        onHtmlChange = {
            onChangeSubmissionText(it)
        },
        onClickToEditInNewScreen = onClickEditSubmission
    )
}