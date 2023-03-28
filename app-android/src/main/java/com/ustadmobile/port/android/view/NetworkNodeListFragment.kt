package com.ustadmobile.port.android.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.material.composethemeadapter.MdcTheme
import com.ustadmobile.core.viewmodel.NetworkNodeListUiState
import com.ustadmobile.port.android.util.ext.defaultScreenPadding


@Composable
private fun NetworkNodeListScreen(
    uiState: NetworkNodeListUiState = NetworkNodeListUiState(),
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {

    }
}

@Composable
@Preview
fun NetworkNodeListPreview() {
    val uiStateVal = NetworkNodeListUiState()

    MdcTheme {
        NetworkNodeListScreen(uiStateVal)
    }
}