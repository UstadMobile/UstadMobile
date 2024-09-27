package com.ustadmobile.libuicompose.view.siteenterlink

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.siteenterlink.LearningSpaceEnterLinkUiState

@Composable
@Preview
fun SiteEnterLinkScreenPreview() {
    LearningSpaceEnterLinkScreen(
        uiState = LearningSpaceEnterLinkUiState()
    )
}