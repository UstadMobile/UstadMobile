package com.ustadmobile.libuicompose.view.site.termsdetail

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.ustadmobile.core.viewmodel.site.termsdetail.SiteTermsDetailUiState
import com.ustadmobile.lib.db.entities.SiteTerms

@Composable
@Preview
fun SiteTermsDetailScreenPreview() {
    val uiState = SiteTermsDetailUiState(
        siteTerms = SiteTerms().apply {
            termsHtml = "https://www.ustadmobile.com"
        },
    )


    SiteTermsDetailScreen(uiState)
}
