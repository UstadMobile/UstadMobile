package com.ustadmobile.port.android.view.clazzassignment.submissionstab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabViewModel
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.libuicompose.view.clazzassignment.submissionstab.ClazzAssignmentDetailSubmissionsTabScreenForViewModel
import com.ustadmobile.port.android.util.compose.courseTerminologyEntryResource
import com.ustadmobile.port.android.util.compose.rememberCourseTerminologyEntries
import com.ustadmobile.port.android.util.ext.getContextSupportFragmentManager
import com.ustadmobile.port.android.view.SortBottomSheetFragment
import com.ustadmobile.port.android.view.composable.UstadListSortHeader
import com.ustadmobile.port.android.view.composable.UstadListSpacerItem
import com.ustadmobile.core.R as CR
import com.ustadmobile.core.MR


class ClazzAssignmentDetailSubmissionsTabFragment: UstadBaseMvvmFragment(){

    private val viewModel by ustadViewModels(::ClazzAssignmentDetailSubmissionsTabViewModel)

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
                    ClazzAssignmentDetailSubmissionsTabScreenForViewModel(viewModel)
                }
            }
        }
    }
}