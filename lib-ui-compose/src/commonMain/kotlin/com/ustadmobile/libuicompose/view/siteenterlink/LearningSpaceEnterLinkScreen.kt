package com.ustadmobile.libuicompose.view.siteenterlink

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.siteenterlink.LearningSpaceEnterLinkUiState
import com.ustadmobile.core.viewmodel.siteenterlink.LearningSpaceEnterLinkViewModel
import com.ustadmobile.libuicompose.components.UstadErrorText
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import com.ustadmobile.libuicompose.images.UstadImage
import com.ustadmobile.libuicompose.images.ustadAppImagePainter
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun LearningSpaceEnterLinkScreen(
    viewModel: LearningSpaceEnterLinkViewModel
) {
    val uiState: LearningSpaceEnterLinkUiState by viewModel.uiState.collectAsStateWithLifecycle(
        initial = LearningSpaceEnterLinkUiState(),  context = Dispatchers.Main.immediate
    )

    LearningSpaceEnterLinkScreen(
        uiState = uiState,
        onClickNext = viewModel::onClickNext,
        onEditTextValueChange = viewModel::onSiteLinkUpdated,
    )
}

@Composable
fun LearningSpaceEnterLinkScreen(
    uiState: LearningSpaceEnterLinkUiState,
    onClickNext: () -> Unit = {},
    onEditTextValueChange: (String) -> Unit = {},
) {


    UstadVerticalScrollColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {
        Spacer(Modifier.height(16.dp))

        Image(
            painter = ustadAppImagePainter(UstadImage.ILLUSTRATION_CONNECT),
            modifier = Modifier.size(156.dp),
            contentDescription = null,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            stringResource(MR.strings.please_enter_the_linK),
            modifier = Modifier.defaultItemPadding()
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding()
                .testTag("site_link")
                ,
            value = uiState.siteLink,
            label = { Text(stringResource(MR.strings.site_link)) },
            onValueChange = {
                onEditTextValueChange(it)
            },
            isError = uiState.linkError != null,
            enabled = uiState.fieldsEnabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(
                onGo = {
                    onClickNext()
                }
            )
        )

        uiState.linkError?.also {
            UstadErrorText(error = it)
        }


        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClickNext,
            enabled = uiState.fieldsEnabled,
            modifier = Modifier
                .testTag("next_button")
                .defaultItemPadding()
                .fillMaxWidth(),
        ) {
            Text(stringResource(MR.strings.next))
        }

        Spacer(modifier = Modifier.height(16.dp))

    }

}