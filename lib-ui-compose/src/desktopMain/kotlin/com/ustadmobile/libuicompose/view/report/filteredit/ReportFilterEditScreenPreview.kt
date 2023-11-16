package com.ustadmobile.libuicompose.view.report.filteredit

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.ReportFilterEditUiState
import com.ustadmobile.lib.db.entities.UidAndLabel


@Composable
@Preview
fun ReportFilterEditScreenPreview() {
    val uiStateVal = ReportFilterEditUiState(
        uidAndLabelList = listOf(
            UidAndLabel().apply {
                uid = 1
                labelName = "First Filter"
            },
            UidAndLabel().apply {
                uid = 2
                labelName = "Second Filter"
            }
        ),
        createNewFilter = "Create new filter",
        reportFilterValueVisible = true
    )

    ReportFilterEditScreen(uiStateVal)

}