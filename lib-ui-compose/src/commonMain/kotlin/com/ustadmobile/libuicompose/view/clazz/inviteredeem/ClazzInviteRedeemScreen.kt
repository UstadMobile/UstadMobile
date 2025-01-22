package com.ustadmobile.libuicompose.view.clazz.inviteredeem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.clazz.inviteredeem.ClazzInviteRedeemViewModel
import com.ustadmobile.core.viewmodel.clazz.inviteredeem.ClazzInviteRedeemUiState
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import dev.icerock.moko.resources.compose.stringResource


@Composable
fun ClazzInviteRedeemScreen(
    viewModel: ClazzInviteRedeemViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ClazzInviteRedeemUiState())

    ClazzInviteRedeemScreen(
        uiState = uiState,
        processDecision = {viewModel.processDecision(it)},
    )
}

@Composable
fun ClazzInviteRedeemScreen(
    uiState: ClazzInviteRedeemUiState = ClazzInviteRedeemUiState(),
    processDecision: (Boolean) -> Unit,
) {
    UstadVerticalScrollColumn(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            uiState.inviteUsed -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(MR.strings.invite_has_been_used))

                    uiState.errorText?.also { errorText ->
                        Text(color = MaterialTheme.colorScheme.error, text = errorText)
                    }
                }
            }

            uiState.showButtons -> {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(MR.strings.do_you_want_to_join_this_course))
                Spacer(modifier = Modifier.height(26.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(onClick = { processDecision(true) } ) {
                        Text(text = stringResource(MR.strings.accept))
                    }

                    OutlinedButton(onClick = { processDecision(false) } ) {
                        Text(text = stringResource(MR.strings.decline))
                    }
                }

                uiState.errorText?.also { errorText ->
                    Text(color = MaterialTheme.colorScheme.error, text = errorText)
                }
            }
        }
    }
}