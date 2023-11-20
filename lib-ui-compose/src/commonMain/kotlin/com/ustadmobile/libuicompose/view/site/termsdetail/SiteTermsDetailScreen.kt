package com.ustadmobile.libuicompose.view.site.termsdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLData
import com.ustadmobile.core.viewmodel.SiteTermsDetailUiState


@Composable
fun SiteTermsDetailScreen(
    uiState: SiteTermsDetailUiState = SiteTermsDetailUiState()
) {

    val state = rememberWebViewStateWithHTMLData(
        data = ""
    )
    val navigator = rememberWebViewNavigator()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
    )  {

        LaunchedEffect(uiState.siteTerms?.termsHtml) {
            navigator.loadHtml(uiState.siteTerms?.termsHtml ?: "")
        }

        WebView(
            state = state,
            navigator = navigator,
        )
    }
}
