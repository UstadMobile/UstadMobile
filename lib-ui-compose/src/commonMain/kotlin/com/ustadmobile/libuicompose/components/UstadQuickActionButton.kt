package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun UstadQuickActionButton(
    imageVector: ImageVector? = null,
    labelText: String,
    onClick: (() -> Unit) = {  },
){

    TextButton(
        modifier = Modifier.width(110.dp),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FilledTonalIconButton(
                onClick = onClick
            ) {
                if (imageVector != null) {
                    Icon(imageVector = imageVector, contentDescription = null)
                }
            }

            Text(
                text = labelText,
                style = MaterialTheme.typography.subtitle1,
                color = contentColorFor(MaterialTheme.colors.secondary)
            )
        }
    }
}