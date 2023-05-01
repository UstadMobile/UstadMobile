package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.android.material.composethemeadapter.MdcTheme
import com.ustadmobile.core.viewmodel.CourseBlockEditUiState
import com.ustadmobile.core.viewmodel.CourseBlockEditViewModel
import com.ustadmobile.port.android.view.composable.UstadCourseBlockEdit

class CourseBlockEditFragment: UstadBaseMvvmFragment() {

    private val viewModel: CourseBlockEditViewModel by ustadViewModels(::CourseBlockEditViewModel)

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
                    CourseBlockEditScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun CourseBlockEditScreen(viewModel: CourseBlockEditViewModel) {
    val uiState: CourseBlockEditUiState by viewModel.uiState.collectAsState(CourseBlockEditUiState())
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {
        UstadCourseBlockEdit(
            uiState = uiState,
            onCourseBlockChange = viewModel::onEntityChanged,
            onClickEditDescription = viewModel::onClickEditDescription,

        )
    }

}
