package com.ustadmobile.port.android.view.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

//As per https://developer.android.com/reference/kotlin/androidx/compose/material/package-summary#Switch(kotlin.Boolean,kotlin.Function1,androidx.compose.ui.Modifier,kotlin.Boolean,androidx.compose.foundation.interaction.MutableInteractionSource,androidx.compose.material.SwitchColors)
@Composable
fun UstadSwitchField(
    checked: Boolean,
    label: String,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .toggleable(
                role = Role.Switch,
                value = checked,
                onValueChange = onChange,
            ),
    ) {
        Text(label)
        Spacer(Modifier.weight(1.0f))
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
        )
    }
}

@Composable
@Preview
fun UstadSwitchRowPreview() {
    var switchRow: Boolean by remember {
        mutableStateOf(true)
    }

    UstadSwitchField(
        checked = switchRow,
        label = "Switch",
        onChange = {
            switchRow = !switchRow
        }
    )
}
