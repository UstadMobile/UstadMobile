package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.libuicompose.view.person.list.PersonListScreenForViewModel

interface InviteWithLinkHandler{
    fun handleClickInviteWithLink()
}

class PersonListFragment() : UstadBaseMvvmFragment() {

    private val viewModel: PersonListViewModel by ustadViewModels { di, savedStateHandle ->
        PersonListViewModel(di, savedStateHandle, requireDestinationViewName())
    }

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
                    PersonListScreenForViewModel(viewModel)
                }
            }
        }
    }


}