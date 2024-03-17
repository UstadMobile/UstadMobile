package com.ustadmobile.libuicompose.view.person.bulkaddrunimport

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.person.bulkaddrunimport.BulkAddPersonRunImportUiState
import com.ustadmobile.core.viewmodel.person.bulkaddrunimport.BulkAddPersonRunImportViewModel
import com.ustadmobile.libuicompose.components.UstadLazyColumn

@Composable
fun BulkAddPersonRunImportScreen(viewModel: BulkAddPersonRunImportViewModel) {
    val uiState by viewModel.uiState.collectAsState(
        BulkAddPersonRunImportUiState())

    BulkAddPersonRunImportScreen(uiState)
}

@Composable
fun BulkAddPersonRunImportScreen(uiState: BulkAddPersonRunImportUiState) {
    when {
        uiState.inProgress -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val progressVal = uiState.progress
                    if(progressVal > 0) {
                        LinearProgressIndicator(
                            progress = progressVal,
                            modifier = Modifier.width(192.dp),
                        )
                    }else {
                        LinearProgressIndicator(
                            modifier = Modifier.width(192.dp),
                        )
                    }

                    Text("Importing")
                }
            }
        }

        uiState.hasErrors -> {
            UstadLazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Icon(Icons.Default.Error, contentDescription = null)
                }

                uiState.errorMessage?.also { errorMessage ->
                    item {
                        Text(errorMessage)
                    }
                }

                items(
                    items = uiState.errors
                ) {
                    Text("${it.lineNum} - ${it.colName} - ${it.invalidValue}")
                }
            }
        }

        else -> {
            Text("Imported: ${uiState.numImported}")
        }
    }
}
