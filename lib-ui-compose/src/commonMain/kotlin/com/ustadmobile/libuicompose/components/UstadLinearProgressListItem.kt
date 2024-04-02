package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun UstadLinearProgressListItem(
    progress: Float?,
    supportingContent: @Composable () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            if(progress != null) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                )
            }else {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                )
            }
        },
        supportingContent = supportingContent,
        trailingContent = {
            UstadTooltipBox(
                tooltipText = stringResource(MR.strings.cancel),
            ) {
                IconButton(
                    onClick = onCancel
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(MR.strings.cancel))
                }
            }
        }
    )
}