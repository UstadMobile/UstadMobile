package com.ustadmobile.libuicompose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun <T> rememberEmptyFlow(): Flow<T>  = remember(Unit) {
    emptyFlow()
}
