package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * This is required because compose/view interop thinks of the bottom nav as visible space
 */
fun LazyListScope.UstadListSpacerItem() {
    item {
        Spacer(modifier = Modifier.height(96.dp))
    }
}
