package com.ustadmobile.libuicompose.view.site.edit

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.site.edit.SiteEditUiState
import com.ustadmobile.lib.db.entities.Language
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage


@Composable
@Preview
fun SiteEditScreenPreview(){
    SiteEditScreen(
        uiState = SiteEditUiState(
            site = Site().apply {
                siteName = "My Site"
            },
            siteTerms = listOf(
                SiteTermsWithLanguage().apply {
                    stLanguage = Language().apply {
                        name = "fa"
                    }
                }
            )
        ),
    )
}