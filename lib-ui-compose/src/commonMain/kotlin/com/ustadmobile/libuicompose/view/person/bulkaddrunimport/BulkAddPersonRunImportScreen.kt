package com.ustadmobile.libuicompose.view.person.bulkaddrunimport

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR

@Composable
fun BulkAddPersonRunImportScreen(viewModel: BulkAddPersonRunImportViewModel) {
    val uiState by viewModel.uiState.collectAsState(
        BulkAddPersonRunImportUiState()
    )

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

                    Text(stringResource(MR.strings.importing))
                }
            }
        }

        uiState.hasErrors -> {
            UstadLazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                uiState.errorMessage?.also { errorMessage ->
                    item {
                        ListItem(
                            leadingContent = {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null)
                            },
                            headlineContent = {
                                Text("${stringResource(MR.strings.error)}: $errorMessage")
                            }
                        )
                    }
                }

                items(
                    items = uiState.errors
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                "${stringResource(MR.strings.error)}: " +
                                    "${stringResource(MR.strings.line_number)} ${it.lineNum} - " +
                                    "${it.colName} - ${it.invalidValue}"
                            )
                        },
                        leadingContent = {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null)
                        },
                    )
                }
            }
        }

        else -> {
            Box(
                modifier = Modifier.fillMaxSize().defaultItemPadding(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.height(8.dp))
                    Text("${stringResource(MR.strings.imported)}: ${uiState.numImported}")
                }
            }
        }
    }
}
