package com.ustadmobile.libuicompose.view.contententry.detailattempttab

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptsSessionListViewModel


@Composable
fun ContentEntryDetailAttemptsSessionListScreen(
    viewModel: ContentEntryDetailAttemptsSessionListViewModel
) {

    ContentEntryDetailAttemptsPersonListScreen ()

}

@Composable
fun ContentEntryDetailAttemptsPersonListScreen() {
    Text("Hello")
}
