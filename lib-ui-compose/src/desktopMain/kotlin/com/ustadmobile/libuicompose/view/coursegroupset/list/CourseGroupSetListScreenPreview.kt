package com.ustadmobile.libuicompose.view.coursegroupset.list

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import app.cash.paging.compose.*
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListUiState

@Composable
@Preview
fun CourseGroupSetListScreenPreview() {
    CourseGroupSetListScreen(
        uiState = CourseGroupSetListUiState(
//            courseGroupSets = {
//                ListPagingSource(listOf(
//                    CourseGroupSet().apply {
//                        cgsName = "Assignment groups"
//                        cgsUid = 1
//                    }
//                ))
//            }
        )
    )
}