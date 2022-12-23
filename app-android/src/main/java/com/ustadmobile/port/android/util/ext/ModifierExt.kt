package com.ustadmobile.port.android.util.ext

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 *
 */
fun Modifier.applyEditAutoPadding(autoPadding: Boolean): Modifier = if(autoPadding) {
    padding(horizontal = 16.dp, vertical = 8.dp)
}else {
    this
}
