package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun SocialWarningListItem(
    onDismiss: () -> Unit,
    onLearnMore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Warning section with a clear caution message
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            headlineContent = {
                Text(
                    text = stringResource(MR.strings.be_careful_interacting_online),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            supportingContent = {
                Text(
                    text = stringResource(MR.strings.be_careful_not_to_share),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(MR.strings.got_it))
                    }
                    TextButton(onClick = onLearnMore) {
                        Text(text = stringResource(MR.strings.learn_more))
                    }
                }
            }
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}
