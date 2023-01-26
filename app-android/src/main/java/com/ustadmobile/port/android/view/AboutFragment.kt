package com.ustadmobile.port.android.view

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ustadmobile.core.viewmodel.AboutUiState

@Composable
fun AboutScreen(
    uiState: AboutUiState
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = uiState.version ?: "",
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        AndroidView(
            modifier = Modifier.padding(top = 8.dp),
            factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
                loadUrl(uiState.url ?: "")
            }
        }, update = {
            it.loadUrl(uiState.url ?: "")
        })
    }
}

@Composable
@Preview
fun AboutScreenPreview(){
    AboutScreen(
        uiState = AboutUiState(
            url = "https://www.ustadmobile.com",
            version = "v0.4.4 (#232) - Thu, 19 Jan 2023 11:00:43 UTC"
        )
    )
}