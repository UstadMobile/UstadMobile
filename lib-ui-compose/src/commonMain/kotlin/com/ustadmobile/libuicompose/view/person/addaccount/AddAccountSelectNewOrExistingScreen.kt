package com.ustadmobile.libuicompose.view.person.addaccount

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.viewmodel.AddAccountSelectNewOrExistingViewModel
import com.ustadmobile.core.viewmodel.AddAccountSelectNewOrExistingUiState
import com.ustadmobile.libuicompose.components.UstadSetLanguageDropDown
import com.ustadmobile.libuicompose.components.UstadWaitForRestartDialog
import com.ustadmobile.libuicompose.images.UstadImage
import com.ustadmobile.libuicompose.images.ustadAppImagePainter
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun AddAccountSelectNewOrExistingScreen(
    viewModel: AddAccountSelectNewOrExistingViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle(AddAccountSelectNewOrExistingUiState())

    if (uiState.showWaitForRestart) {
        UstadWaitForRestartDialog()
    }

    AddAccountSelectNewOrExistingScreen(
        uiState = uiState,
        onSetLanguage = viewModel::onLanguageSelected,
        onClickNewUser = viewModel::onClickNewUser,
        onClickExistingUser = viewModel::onClickExistingUser,
        onClickBadgeQrCode = viewModel::onClickBadgeQrCode,
    )
}


@Composable
fun AddAccountSelectNewOrExistingScreen(
    uiState: AddAccountSelectNewOrExistingUiState,
    onSetLanguage: (UstadMobileSystemCommon.UiLanguage) -> Unit = { },
    onClickNewUser: () -> Unit = { },
    onClickExistingUser: () -> Unit = { },
    onClickBadgeQrCode: () -> Unit = { },
) {

    Column(
        modifier = Modifier.fillMaxHeight().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            TopRow(
                uiState, onSetLanguage = onSetLanguage
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = ustadAppImagePainter(UstadImage.APP_LOGO),
            contentDescription = "",
            modifier = Modifier.height(100.dp).fillMaxWidth()
                .padding(horizontal = 20.dp),

            )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(MR.strings.app_name),
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClickNewUser,
            modifier = Modifier.fillMaxWidth().defaultItemPadding()
        ) {
            Text(stringResource(MR.strings.new_user))
        }

        Button(
            onClick = onClickExistingUser,
            modifier = Modifier.fillMaxWidth().defaultItemPadding()
        ) {
            Text(stringResource(MR.strings.existing_user))
        }
        Button(
            onClick = onClickBadgeQrCode,
            modifier = Modifier.fillMaxWidth().defaultItemPadding()
        ) {
            Text(stringResource(MR.strings.scan_badge_qr_code))
        }
    }
}

@Composable
private fun TopRow(
    uiState: AddAccountSelectNewOrExistingUiState,
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





