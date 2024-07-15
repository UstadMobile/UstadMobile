package com.ustadmobile.libuicompose.view.clazz.redeem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.clazz.redeem.ClazzInviteViewModel
import com.ustadmobile.libuicompose.components.UstadVerticalScrollColumn


@Composable
fun ClazzInviteRedeem(
    viewModel: ClazzInviteViewModel
) {

    InviteViaLinkScreen(
        onAccept = {},
        onDecline = {}
    )
}

@Composable
fun InviteViaLinkScreen(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    UstadVerticalScrollColumn(
        modifier = Modifier
            .fillMaxSize(),

        ) {
        Text(text = "Do you want to join this course?")
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = onAccept) {
                Text(text = "Accept")
            }
            Button(onClick = onDecline) {
                Text(text = "Decline")
            }
        }
    }
}