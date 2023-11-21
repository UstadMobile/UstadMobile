package com.ustadmobile.libuicompose.view.clazz.joinwithcode

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.JoinWithCodeUiState


@Composable
@Preview
fun JoinWithCodeScreenPreview(){
    JoinWithCodeScreen(
        uiState = JoinWithCodeUiState(
            entityType = "Course",
            buttonLabel = "Join course"
        )
    )
}