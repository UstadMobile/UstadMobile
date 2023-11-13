package com.ustadmobile.port.android.view.clazzassignment.submitterdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.accompanist.themeadapter.material.MdcTheme
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.core.viewmodel.clazzassignment.submitterdetail.ClazzAssignmentSubmitterDetailViewModel
import com.ustadmobile.libuicompose.view.clazzassignment.submitterdetail.ClazzAssignmentDetailStudentProgressScreenForViewModel

interface ClazzAssignmentDetailStudentProgressFragmentEventHandler {

    fun onSubmitGradeClicked()

    fun onSubmitGradeAndMarkNextClicked()

}

class ClazzAssignmentSubmitterDetailFragment: UstadBaseMvvmFragment() {

    private val viewModel by ustadViewModels(::ClazzAssignmentSubmitterDetailViewModel)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    ClazzAssignmentDetailStudentProgressScreenForViewModel(viewModel)
                }
            }
        }
    }

}