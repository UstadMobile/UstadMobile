package com.ustadmobile.libuicompose.view.clazz.invitevialink

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.clazz.invitevialink.InviteViaLinkUiState
import com.ustadmobile.core.viewmodel.clazz.invitevialink.InviteViaLinkViewModel
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource


@Composable
fun InviteViaLinkScreen(
    viewModel: InviteViaLinkViewModel
) {
    val uiState by viewModel.uiState.collectAsState(InviteViaLinkUiState())

    InviteViaLinkScreen(
        uiState = uiState,
        onClickCopyLink = viewModel::onClickCopy,
        onClickShareLink = viewModel::onClickShare,
    )
}

@Composable
fun InviteViaLinkScreen(
    uiState: InviteViaLinkUiState = InviteViaLinkUiState(),
    onClickCopyLink: () -> Unit = {},
    onClickShareLink: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    )  {

        Text(
            stringResource(MR.strings.invite_link_desc),
            modifier = Modifier.defaultItemPadding(),
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().testTag("course_code").defaultItemPadding(),
            value = uiState.inviteLink ?: "",
            readOnly = true,
            maxLines = 1,
            onValueChange = { }
        )

        Row(
            modifier = Modifier.defaultItemPadding()
        ) {
            OutlinedButton(
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = onClickCopyLink,
            ){
                Text(stringResource(MR.strings.copy_link))
            }

            if(uiState.showShareLinkButton) {
                OutlinedButton(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = onClickShareLink
                ) {
                    Text(stringResource(MR.strings.share_link))
                }
            }
        }
    }
}
