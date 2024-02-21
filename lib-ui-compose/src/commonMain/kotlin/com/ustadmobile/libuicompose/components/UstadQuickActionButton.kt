package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun UstadQuickActionButton(
    iconContent: (@Composable () -> Unit)? = null,
    labelText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource =  remember { MutableInteractionSource() }

    TextButton(
        modifier = modifier.width(112.dp),
        onClick = onClick,
        interactionSource = interactionSource,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FilledTonalIconButton(
                onClick = onClick,
                interactionSource = interactionSource,
            ) {
                iconContent?.invoke()
            }

            Text(
                text = labelText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.background),
            )
        }
    }
}

@Composable
fun UstadQuickActionButton(
    imageVector: ImageVector? = null,
    labelText: String,
    onClick: (() -> Unit) = {  },
){
    UstadQuickActionButton(
        iconContent = {
            if (imageVector != null) {
                Icon(imageVector = imageVector, contentDescription = null)
            }
        },
        labelText = labelText,
        onClick = onClick
    )
}