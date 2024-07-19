package com.ustadmobile.libuicompose.view.clazz.redeem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.InviteViaContactUiState
import com.ustadmobile.core.viewmodel.clazz.redeem.ClazzInviteViewModel
import com.ustadmobile.core.viewmodel.clazz.redeem.InviteRedeemUiState
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn
import dev.icerock.moko.resources.compose.stringResource


@Composable
fun ClazzInviteRedeem(
    viewModel: ClazzInviteViewModel
) {
    val uiState by viewModel.uiState.collectAsState(InviteRedeemUiState())

    InviteViaLinkScreen(
        uiState = uiState,
        processDecision = {viewModel.processDecision(it)},
    )
}

@Composable
fun InviteViaLinkScreen(
    uiState: InviteRedeemUiState = InviteRedeemUiState(),
    processDecision: (Boolean) -> Unit,
) {
    UstadVerticalScrollColumn(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally


    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(MR.strings.do_you_want_to_join_this_course))
        Spacer(modifier = Modifier.height(26.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = { processDecision(true)}) {
                Text(text = stringResource(MR.strings.accept))

            }
            Button(onClick = { processDecision(false) }) {
                Text(text = stringResource(MR.strings.decline))
            }
        }
    }
}