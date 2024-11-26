package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun SocialWarningListItem(
    onDismiss: () -> Unit,
    onLearnMore: () -> Unit
) {
    ListItem(
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null
            )
        },
        headlineContent = {
            Text(
                text = stringResource(MR.strings.be_careful_interacting_online)
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = stringResource(MR.strings.be_careful_not_to_share)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(MR.strings.got_it)
                        )
                    }
                    TextButton(onClick = onLearnMore) {
                        Text(
                            text = stringResource(MR.strings.learn_more)
                        )
                    }
                }
            }
        }
    )
}
