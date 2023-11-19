package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * This is conceptually similar to a ListItem. It does not set any background
 */
@Composable
fun UstadDetailField2(
    valueContent: @Composable () -> Unit,
    labelContent: (@Composable () -> Unit)? = null,
    leadingContent: @Composable (()-> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ){
            if(leadingContent != null)
                leadingContent()

            //Note: If needed to set color - use
            // CompositionLocalProvider(LocalContentColor provides color)

            Column(
                modifier = if(leadingContent != null) {
                    Modifier.padding(start = 16.dp).weight(1f)
                }else {
                    Modifier
                }
            ) {
                ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
                    valueContent()
                }

                Spacer(Modifier.height(8.dp))

                if(labelContent != null) {
                    ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                        labelContent()
                    }
                }
            }


            if(trailingContent != null) {
                Box(contentAlignment = Alignment.TopEnd) {
                    trailingContent()
                }
            }
        }
    }
}

@Composable
fun UstadDetailField2(
    valueText: String,
    labelText: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    UstadDetailField2(
        modifier = modifier,
        valueContent = { Text(valueText) },
        labelContent = { Text(labelText) },
        leadingContent = if(icon != null) {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null
                )
            }
        }else {
            null
        }
    )
}