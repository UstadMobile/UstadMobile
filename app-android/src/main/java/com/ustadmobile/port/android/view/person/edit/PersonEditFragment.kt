package com.ustadmobile.port.android.view.person.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.person.edit.PersonEditUiState
import com.ustadmobile.core.viewmodel.person.edit.PersonEditViewModel
import com.ustadmobile.libuicompose.view.person.edit.PersonEditScreenForViewModel
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment

class PersonEditFragment: UstadBaseMvvmFragment() {

    private val viewModel: PersonEditViewModel by ustadViewModels(::PersonEditViewModel)
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
                    PersonEditScreenForViewModel(viewModel)
                }
            }
        }
    }


}

@Preview
@Composable
private fun PersonEditPreview() {
    val uiState = PersonEditUiState()
//    PersonEditScreen(uiState)
}