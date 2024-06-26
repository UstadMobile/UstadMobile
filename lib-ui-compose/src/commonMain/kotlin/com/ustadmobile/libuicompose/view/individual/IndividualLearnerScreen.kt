package com.ustadmobile.libuicompose.view.individual


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.individual.IndividualLearnerViewModel
import com.ustadmobile.libuicompose.components.UstadPickFileOpts
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
    uiState.selectedFileName?.let { fileName ->
        Text("Selected file: $fileName")
    }

    Column(
        modifier = Modifier.fillMaxHeight().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = ustadAppImagePainter(UstadImage.ILLUSTRATION_CONNECT),
            contentDescription = "",
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(MR.strings.create_new_local_account_title),
                            fontSize = 16.sp
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(MR.strings.create_new_local_account_description),
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.clickable { viewModel.onClickContinueWithoutLogin() }
                )
                HorizontalDivider()
            }
            item {
                ListItem(
                    headlineContent = {
                        Text(
                            text = stringResource(MR.strings.restore_local_account_title),
                            fontSize = 16.sp
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(MR.strings.restore_local_account_description),
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.clickable {
                        filePickLauncher(UstadPickFileOpts())
                    }
                )
                HorizontalDivider()
            }
        }
    }
}