package com.ustadmobile.port.android.view.clazzassignment.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.core.viewmodel.clazzassignment.edit.ClazzAssignmentEditViewModel
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.libuicompose.view.clazzassignment.edit.ClazzAssignmentEditScreenForViewModel

class ClazzAssignmentEditFragment: UstadBaseMvvmFragment(){

    private val viewModel: ClazzAssignmentEditViewModel by
        ustadViewModels(::ClazzAssignmentEditViewModel)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    ClazzAssignmentEditScreenForViewModel(viewModel)
                }
            }
        }
    }
}

//@Composable
//@Preview
//fun ClazzAssignmentEditScreenPreview() {
//    val uiStateVal = ClazzAssignmentEditUiState(
//        courseBlockEditUiState = CourseBlockEditUiState(
//            courseBlock = CourseBlock().apply {
//                cbMaxPoints = 78
//                cbCompletionCriteria = 14
//            },
//        ),
//        entity = CourseBlockWithEntity().apply {
//            assignment = ClazzAssignment().apply {
//                caMarkingType = ClazzAssignment.MARKED_BY_PEERS
//            }
//        }
//    )
//
//    ClazzAssignmentEditScreen(uiStateVal)
//}