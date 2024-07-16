package com.ustadmobile.libuicompose.view.individual


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.individual.ExtractionStatus
import com.ustadmobile.core.viewmodel.individual.IndividualLearnerViewModel
import com.ustadmobile.libuicompose.components.PickFileOptions
import com.ustadmobile.libuicompose.components.PickType
import com.ustadmobile.libuicompose.components.rememberUstadFilePickLauncher
import com.ustadmobile.libuicompose.images.UstadImage
import com.ustadmobile.libuicompose.images.ustadAppImagePainter
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun IndividualLearnerScreen(viewModel: IndividualLearnerViewModel) {
    IndividualLearnerScreenContent(viewModel)
}

@Composable
fun IndividualLearnerScreenContent(viewModel: IndividualLearnerViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val filePickLauncher = rememberUstadFilePickLauncher { result ->
        viewModel.onRestoreFileSelected(fileUri = result.uri, fileName = result.fileName)
    }

    Column(
        modifier = Modifier.fillMaxHeight().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ListItem(
                    leadingContent = {
                        Image(
                            painter = ustadAppImagePainter(UstadImage.INDIVIDUAL_NEW_ACCOUNT),
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                        )
                    },
                    headlineContent = {
                        Text(
                            text = stringResource(MR.strings.create_new_local_account_title),
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(MR.strings.create_new_local_account_description),
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        )


                    },
                    modifier = Modifier.clickable { viewModel.onClickContinueWithoutLogin() }
                )
                HorizontalDivider()
            }
            item {
                ListItem(
                    leadingContent = {
                        Image(
                            painter = ustadAppImagePainter(UstadImage.INDIVIDUAL_RESTORE_ACCOUNT),
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                        )
                    },

                    headlineContent = {
                        Text(
                            text = stringResource(MR.strings.restore_local_account_title),
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(MR.strings.restore_local_account_description),
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        )

                    },
                    modifier = Modifier.clickable {
                        filePickLauncher(PickFileOptions(pickType = PickType.FILE))
                    }
                )

                if (uiState.selectedFileName != null) {
                    Column(
                        modifier = Modifier.fillMaxHeight().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    ) {
                        Text(
                            text = uiState.selectedFileName ?: "",
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (uiState.extractionStatus) {
                                ExtractionStatus.Idle -> "Ready to extract"
                                ExtractionStatus.Extracting -> "Extracting..."
                                ExtractionStatus.Completed -> "Extraction completed"
                                ExtractionStatus.Error -> "Extraction failed"
                            },
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            color = when (uiState.extractionStatus) {
                                ExtractionStatus.Extracting -> Color.Blue
                                ExtractionStatus.Completed -> Color.Green
                                ExtractionStatus.Error -> Color.Red
                                else -> Color.Gray
                            }
                        )
                        if (uiState.extractionStatus == ExtractionStatus.Extracting) {
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = uiState.extractionProgress,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                HorizontalDivider()
            }

        }
    }
}