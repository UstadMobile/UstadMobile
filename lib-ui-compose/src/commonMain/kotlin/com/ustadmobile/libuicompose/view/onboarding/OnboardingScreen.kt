package com.ustadmobile.libuicompose.view.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DomainAdd
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.viewmodel.OnBoardingViewModel
import com.ustadmobile.core.viewmodel.OnboardingUiState
import com.ustadmobile.libuicompose.components.UstadSetLanguageDropDown
import com.ustadmobile.libuicompose.components.UstadWaitForRestartDialog
import com.ustadmobile.libuicompose.images.UstadImage
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun OnboardingScreen(
    viewModel: OnBoardingViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(OnboardingUiState())

    if (uiState.showWaitForRestart) {
        UstadWaitForRestartDialog()
    }

    OnboardingScreen(
        uiState = uiState,
        onSetLanguage = viewModel::onLanguageSelected,
        onClickNext = viewModel::onClickExistJoining,
        onClickIndividual = viewModel::onClickIndividual,
        onClickAddNewOrganization = viewModel::onClickAddNewOrganization,
    )
}



@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onSetLanguage: (UstadMobileSystemCommon.UiLanguage) -> Unit = { },
    onClickNext: () -> Unit = { },
    onClickIndividual: () -> Unit = { },
    onClickAddNewOrganization: () -> Unit = { },
) {

    Column(
        modifier = Modifier.fillMaxHeight().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            TopRow(
                uiState, onSetLanguage = onSetLanguage
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hello",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        Box {
            BottomRow(
                onClickNext,
                onClickIndividual,
                onClickAddNewOrganization,
            )
        }
    }
}

@Composable
private fun TopRow(
    uiState: OnboardingUiState,
    onSetLanguage: (UstadMobileSystemCommon.UiLanguage) -> Unit = { },
) {
    Row(
        modifier = Modifier.wrapContentHeight().fillMaxWidth()
    ) {
        Box {
            UstadSetLanguageDropDown(
                langList = uiState.languageList,
                currentLanguage = uiState.currentLanguage,
                onItemSelected = onSetLanguage
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}



@Composable
private fun BottomRow(
    onClickJoinOrganization: () -> Unit = {},
    onClickIndividual: () -> Unit = {},
    onClickAddOrganization: () -> Unit = {}
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        item {
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(
                        text = stringResource(MR.strings.i_want_to_join_my_organization_school),
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(MR.strings.i_want_to_join_my_organization_school_description),
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    )
                },
                modifier = Modifier.clickable { onClickJoinOrganization() }
            )
            HorizontalDivider()
        }
        item {
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.DomainAdd,
                        contentDescription = null,

                    )
                },
                headlineContent = {
                    Text(
                        text = stringResource(MR.strings.i_want_to_add_my_organization_school),
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(MR.strings.i_want_to_add_my_organization_school_description),
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    )
                },
                modifier = Modifier.clickable {
                    onClickAddOrganization()
                }
            )
            HorizontalDivider()
        }

        item {
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,

                    )
                },
                headlineContent = {
                    Text(
                        text = stringResource(MR.strings.im_an_individual_learner),
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    )
                },
                supportingContent = {
                    Text(
                        text = stringResource(MR.strings.im_an_individual_learner_description),
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    )
                },
                modifier = Modifier.clickable {
                    onClickIndividual()
                }
            )
        }
    }

}


