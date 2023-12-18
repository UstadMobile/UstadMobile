package com.ustadmobile.libuicompose.view.site.detail

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.site.detail.SiteDetailUiState
import com.ustadmobile.lib.db.composites.SiteTermsAndLangName
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTerms

@Composable
@Preview
fun SiteDetailScreenPreview(){
    SiteDetailScreen(
        uiState = SiteDetailUiState(
            site = Site().apply {
                siteName = "My Site"
            },
            siteTerms = listOf(
                SiteTermsAndLangName(
                    terms = SiteTerms(),
                    langDisplayName = "Dari"
                )
            )
        ),
    )
}
