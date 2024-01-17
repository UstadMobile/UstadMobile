package com.ustadmobile.libuicompose.view.contententry.getmetadata

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.contententry.getmetadata.ContentEntryGetMetadataUiState
import com.ustadmobile.core.viewmodel.contententry.getmetadata.ContentEntryGetMetadataViewModel

@Composable
fun ContentEntryGetMetadataScreen(
    uiState: ContentEntryGetMetadataUiState
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val errorVal = uiState.status.error
            if(errorVal == null) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                )
            }else {
                Icon(Icons.Default.Error, contentDescription = null)
                Text(errorVal)
            }
        }

    }

}

@Composable
fun ContentEntryGetMetadataScreen(
    viewModel: ContentEntryGetMetadataViewModel
)  {
    val uiState by viewModel.uiState.collectAsState(
        ContentEntryGetMetadataUiState()
    )

    ContentEntryGetMetadataScreen(uiState)
}
