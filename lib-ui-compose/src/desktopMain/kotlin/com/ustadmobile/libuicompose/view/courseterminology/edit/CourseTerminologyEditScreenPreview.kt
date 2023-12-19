package com.ustadmobile.libuicompose.view.courseterminology.edit

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.impl.locale.TerminologyEntry
import com.ustadmobile.core.viewmodel.courseterminology.edit.CourseTerminologyEditUiState
import com.ustadmobile.core.MR

@Composable
@Preview
fun CourseTerminologyEditScreenPreview() {
    val uiState = CourseTerminologyEditUiState(
        terminologyTermList = listOf(
            TerminologyEntry(
                id = "1",
                term = "First",
                stringResource = MR.strings.teacher
            ),
            TerminologyEntry(
                id = "2",
                term = "Second",
                stringResource = MR.strings.student
            ),
            TerminologyEntry(
                id = "3",
                term = "Third",
                stringResource = MR.strings.add_a_teacher
            )
        )
    )
    CourseTerminologyEditScreen(uiState)

}
