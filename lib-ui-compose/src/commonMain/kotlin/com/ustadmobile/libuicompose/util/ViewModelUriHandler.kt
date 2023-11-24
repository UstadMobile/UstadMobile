package com.ustadmobile.libuicompose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.UriHandler
import com.ustadmobile.core.viewmodel.UstadViewModel

@Composable
fun rememberViewModelUriHandler(viewModel: UstadViewModel): UriHandler {
    return remember(viewModel) {
        ViewModelUriHandler(viewModel)
    }
}

class ViewModelUriHandler(private val viewModel: UstadViewModel): UriHandler {

    override fun openUri(uri: String) {
        viewModel.onClickLink(uri)
    }

}