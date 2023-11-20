package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Existing modal bottom sheet does not have padding at the bottom, which results in options being
 * overlaid by the softkey buttons on Android. This will be expect-actual to avoid adding any extra
 * space on Desktop
 */
@Composable
fun UstadBottomSheetSpacer() {
    Spacer(Modifier.height(48.dp))
}