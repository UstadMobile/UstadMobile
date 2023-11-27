package com.ustadmobile.libuicompose.util

import androidx.compose.runtime.Composable

/**
 * Effect that will hide the soft keyboard when disposed. Has no effect on Desktop.
 */
@Composable
expect fun HideSoftInputEffect()
