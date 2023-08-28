package com.ustadmobile.libuicompose.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import com.ustadmobile.core.impl.locale.StringResources

private val emptyStringResources = object: StringResources {
    override fun get(messageId: Int): String {
        return "MessageId#$messageId"
    }
}

//As per: https://developer.android.com/jetpack/compose/compositionlocal
internal val LocalStringResources = compositionLocalOf<StringResources?> {
    null
}

@Composable
fun withStringResources(
    stringResources: StringResources,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalStringResources provides stringResources) {
        content()
    }
}

@Composable
fun localStringResources(): StringResources {
    return LocalStringResources.current ?: emptyStringResources
}
