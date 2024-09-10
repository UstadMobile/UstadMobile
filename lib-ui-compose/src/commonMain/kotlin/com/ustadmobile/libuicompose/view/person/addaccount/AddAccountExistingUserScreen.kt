package com.ustadmobile.libuicompose.view.ExistingUser

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.AddAccountExistingUserViewModel
import com.ustadmobile.core.viewmodel.AddAccountExistingUserUiState
import com.ustadmobile.libuicompose.images.UstadImage
import com.ustadmobile.libuicompose.images.ustadAppImagePainter
import dev.icerock.moko.resources.compose.stringResource
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun AddAccountExistingUserScreen(
    viewModel: AddAccountExistingUserViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(AddAccountExistingUserUiState())



    AddAccountExistingUserScreen(
        uiState = uiState,
        onClickIndividual = viewModel::onClickIndividual,
        onClickLearningSpace = viewModel::onClickLearningSpace,
    )
}



@Composable
fun AddAccountExistingUserScreen(
    uiState: AddAccountExistingUserUiState,
    onClickIndividual: () -> Unit = { },
    onClickLearningSpace: () -> Unit = { },
) {

    Column(
        modifier = Modifier.fillMaxHeight().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Column(
            modifier = Modifier.padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

        }


        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {

            item {
                ListItem(
                    leadingContent = {
                        Image(
                            painter = ustadAppImagePainter(UstadImage.ONBOARDING_EXISTING),
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                        )
                    },
                    headlineContent = {
                        Text(
                            text = stringResource(MR.strings.individual),
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(MR.strings.access_educational_content_download_offline),
                        )
                    },
                    modifier = Modifier.clickable { onClickIndividual() }
                )
                HorizontalDivider()
            }
            item {
                ListItem(
                    leadingContent = {
                        Image(
                            painter = ustadAppImagePainter(UstadImage.ONBOARDING_ADD_ORG),
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                        )
                    },
                    headlineContent = {
                        Text(
                            text = stringResource(MR.strings.learning_space),
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(MR.strings.eg_for_your_school_organization),
                        )
                    },
                    modifier = Modifier.clickable {
                        onClickLearningSpace()
                    }
                )
                HorizontalDivider()
            }
        }
    }
}





