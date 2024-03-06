package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ustadmobile.core.util.ext.hasFlag
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun UstadPermissionEdit(
    value: Long,
    permissionLabels: List<Pair<StringResource, Long>>,
    onToggle: (Long) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        permissionLabels.forEach {  permissionLabel ->
            UstadSwitchField(
                checked = value.hasFlag(permissionLabel.second),
                label = stringResource(permissionLabel.first),
                onChange = {
                    onToggle(permissionLabel.second)
                },
                modifier = Modifier.defaultItemPadding(),
                enabled = enabled,
            )
        }
    }
}