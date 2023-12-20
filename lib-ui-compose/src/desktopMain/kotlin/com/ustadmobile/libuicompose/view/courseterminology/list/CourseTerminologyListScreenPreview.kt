package com.ustadmobile.libuicompose.view.courseterminology.list

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.courseterminology.list.CourseTerminologyListUiState
import com.ustadmobile.lib.db.entities.CourseTerminology


@Composable
@Preview
fun CourseTerminologyListScreenPreview() {
    CourseTerminologyListScreen(
        uiState = CourseTerminologyListUiState(
            showAddItemInList = true,
            terminologyList = {
                ListPagingSource(listOf(
                    CourseTerminology().apply {
                        ctUid = 1
                        ctTitle = "English"
                    }
                ))
            }
        )
    )
}
