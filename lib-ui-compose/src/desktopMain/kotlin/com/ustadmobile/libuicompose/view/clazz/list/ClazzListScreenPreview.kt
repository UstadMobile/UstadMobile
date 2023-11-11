package com.ustadmobile.libuicompose.view.clazz.list

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import androidx.compose.foundation.layout.*
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListUiState

@Composable
@Preview
fun ClazzListScreenPreview() {

    ClazzListScreen(
        uiState = ClazzListUiState(
//            clazzList = {
//                ListPagingSource(
//                    listOf(
//                        ClazzWithListDisplayDetails().apply {
//                            clazzUid = 1
//                            clazzName = "Class Name"
//                            clazzDesc = "Class Description"
//                            attendanceAverage = 0.3F
//                            numTeachers = 3
//                            numStudents = 2
//                        },
//                        ClazzWithListDisplayDetails().apply {
//                            clazzUid = 2
//                            clazzName = "Class Name"
//                            clazzDesc = "Class Description"
//                            attendanceAverage = 0.3F
//                            numTeachers = 3
//                            numStudents = 2
//                        }
//                    )
//                )
//            },
        )
    )
}