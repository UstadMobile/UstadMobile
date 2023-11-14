package com.ustadmobile.libuicompose.view.language.edit

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.LanguageEditUiState
import com.ustadmobile.lib.db.entities.Language


@Composable
@Preview
fun LanguageEditScreenPreview(){
    LanguageEditScreen(
        uiState = LanguageEditUiState(
            language = Language().apply {
                name = "fa"
            }
        )
    )
}