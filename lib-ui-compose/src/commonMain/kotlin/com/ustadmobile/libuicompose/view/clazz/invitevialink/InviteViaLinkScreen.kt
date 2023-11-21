package com.ustadmobile.libuicompose.view.clazz.invitevialink

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.InviteViaLinkUiState
import dev.icerock.moko.resources.compose.stringResource


@Composable
fun InviteViaLinkScreen(
    uiState: InviteViaLinkUiState = InviteViaLinkUiState(),
    onClickCopyLink: () -> Unit = {},
    onClickShareLink: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )  {

        Text(stringResource(MR.strings.invite_link_desc, uiState.entityName))

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.inviteLink ?: "",
            readOnly = true,
            onValueChange = { }
        )

        Row(
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = onClickCopyLink,
            ){
                Text(stringResource(MR.strings.copy_link))
            }

            TextButton(
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = onClickShareLink
            ) {
                Text(stringResource(MR.strings.share_link))
            }
        }
    }
}
