package com.ustadmobile.libuicompose.locale

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ustadmobile.core.impl.locale.StringResources
import com.ustadmobile.core.impl.locale.StringResourcesAware

private fun Context.getStringResourcesContext(): StringResources = when(this) {
    is StringResourcesAware -> this.stringResources
    is ContextWrapper -> this.getStringResourcesContext()
    else -> throw IllegalArgumentException("getStringResourcesContext(): Cannot find StringResources in context")
}

@Composable
fun androidContextStringResources(): StringResources {
    return LocalContext.current.getStringResourcesContext()
}
