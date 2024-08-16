package com.ustadmobile.libuicompose.view.person.list

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.person.list.PersonListUiState
@Preview
@Composable
private fun PersonEditPreview() {
    PersonListScreen(
        uiState = PersonListUiState(
//            personList = {
//                ListPagingSource(listOf(
//                    PersonWithDisplayDetails().apply {
//                        firstNames = "Ahmad"
//                        lastName = "Ahmadi"
//                        admin = true
//                        personUid = 3
//                    }
//                ))
//            }
        )
    )
}