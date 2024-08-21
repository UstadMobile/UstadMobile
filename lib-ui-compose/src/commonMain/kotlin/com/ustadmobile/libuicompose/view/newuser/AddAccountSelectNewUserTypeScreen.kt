package com.ustadmobile.libuicompose.view.newuser

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.AddAccountSelectNewUserTypeUiState
import com.ustadmobile.core.viewmodel.AddAccountSelectNewUserTypeViewModel
import com.ustadmobile.libuicompose.images.UstadImage
import com.ustadmobile.libuicompose.images.ustadAppImagePainter
import dev.icerock.moko.resources.compose.stringResource
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle


@Composable
fun AddAccountSelectNewUserTypeScreen(
    viewModel: AddAccountSelectNewUserTypeViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(AddAccountSelectNewUserTypeUiState())

    AddAccountSelectNewUserTypeScreen(
        uiState = uiState,
        onClickPersonalAccount = viewModel::onClickPersonalAccount,
        onClickJoinLearningSpace = viewModel::onClickJoinLearningSpace,
        onClickNewLearningSpace = viewModel::onClickNewLearningSpace,
    )
}


@Composable
fun AddAccountSelectNewUserTypeScreen(
    uiState: AddAccountSelectNewUserTypeUiState,
    onClickPersonalAccount: () -> Unit = { },
    onClickJoinLearningSpace: () -> Unit = { },
    onClickNewLearningSpace: () -> Unit = { },
) {

    Column(
        modifier = Modifier.fillMaxHeight().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.showAddPersonalAccount) {
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
                                text = stringResource(MR.strings.personal_account),
                            )
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(MR.strings.access_educational_content_download_offline),
                            )
                        },
                        modifier = Modifier.padding(8.dp)
                            .clickable { onClickPersonalAccount() }

                    )
                    HorizontalDivider()
                }
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
                            text = stringResource(MR.strings.join_learning_space),
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(MR.strings.eg_for_your_school_organization),
                            fontSize = 12.sp,
                        )
                    },
                    modifier = Modifier.padding(8.dp).clickable {
                        onClickJoinLearningSpace()
                    }
                )
                HorizontalDivider()
            }
            item {
                ListItem(
                    leadingContent = {
                        Image(
                            painter = ustadAppImagePainter(UstadImage.ONBOARDING_INDIVIDUAL),
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                        )
                    },
                    headlineContent = {
                        Text(
                            text = stringResource(MR.strings.new_learning_space),
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "NA",
                            fontSize = 12.sp,
                        )
                    },
                    modifier = Modifier.padding(8.dp).clickable {
                        onClickNewLearningSpace()
                    }
                )
            }
        }
    }
}






