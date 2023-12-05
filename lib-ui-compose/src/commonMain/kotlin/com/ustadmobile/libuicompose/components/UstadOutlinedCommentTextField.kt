package com.ustadmobile.libuicompose.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.core.MR
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun UstadOutlinedCommentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmitComment: () -> Unit,
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        enabled = enabled,
        trailingIcon = if(value.isNotBlank()) {
            {
                IconButton(
                    onClick = onSubmitComment,
                    enabled = enabled,
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = stringResource(MR.strings.send)
                    )
                }
            }
        }else {
            null
        }
    )
}
