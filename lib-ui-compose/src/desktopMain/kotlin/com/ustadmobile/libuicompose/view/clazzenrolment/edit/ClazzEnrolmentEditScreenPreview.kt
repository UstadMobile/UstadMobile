package com.ustadmobile.libuicompose.view.clazzenrolment.edit

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.clazzenrolment.edit.ClazzEnrolmentEditUiState
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason

@Composable
@Preview
fun ClazzEnrolmentEditScreenPreview() {
    val uiState = ClazzEnrolmentEditUiState(
        clazzEnrolment = ClazzEnrolmentWithLeavingReason().apply {
            clazzEnrolmentOutcome = ClazzEnrolment.OUTCOME_GRADUATED
        },
    )

    ClazzEnrolmentEditScreen(uiState)
}