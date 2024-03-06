package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import com.ustadmobile.core.util.ext.hasFlag
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR

fun LazyListScope.UstadPermissionLabelsListItems(
    permissionLabels: List<Pair<StringResource, Long>>,
    value: Long,
) {
    items(
        items = permissionLabels,
        key = { it.first }
    ) {
        ListItem(
            headlineContent = { Text(stringResource(it.first)) },
            leadingContent = {
                if(value.hasFlag(it.second)) {
                    Icon(Icons.Default.Check, contentDescription = stringResource(MR.strings.enabled))
                }else {
                    Icon(Icons.Default.Close, contentDescription = stringResource(MR.strings.disabled))
                }
            }
        )
    }
}