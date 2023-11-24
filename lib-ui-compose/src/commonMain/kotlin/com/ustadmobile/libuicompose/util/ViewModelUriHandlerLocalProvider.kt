package com.ustadmobile.libuicompose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import com.ustadmobile.core.viewmodel.UstadViewModel

/**
 * Uses the openLink function on the given ViewModel to provide the LocalUriHandler to be triggered
 * when the user clicks a link
 */
@Composable
fun ViewModelUriHandlerLocalProvider(
    viewModel: UstadViewModel,
    content: @Composable () -> Unit,
) {
    val uriHandler: UriHandler = rememberViewModelUriHandler(viewModel)

    CompositionLocalProvider(LocalUriHandler provides uriHandler) {
        content()
    }
}