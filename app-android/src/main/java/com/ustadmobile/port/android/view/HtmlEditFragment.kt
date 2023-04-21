package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.android.material.composethemeadapter.MdcTheme
import com.ustadmobile.core.viewmodel.HtmlEditUiState
import com.ustadmobile.core.viewmodel.HtmlEditViewModel
import com.ustadmobile.port.android.view.composable.AztecEditor

class HtmlEditFragment: UstadBaseMvvmFragment() {

    val viewModel: HtmlEditViewModel by ustadViewModels(::HtmlEditViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    HtmlEditScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun HtmlEditScreen(viewModel: HtmlEditViewModel) {

    val uiState by viewModel.uiState.collectAsState(initial = HtmlEditUiState())


    AztecEditor(
        html = uiState.html,
        onChange = viewModel::onHtmlChanged,
        modifier = Modifier.fillMaxSize()
    )

}