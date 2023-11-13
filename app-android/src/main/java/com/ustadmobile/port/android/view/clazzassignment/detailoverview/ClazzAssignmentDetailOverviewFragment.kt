package com.ustadmobile.port.android.view.clazzassignment.detailoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.viewmodel.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewViewModel
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import com.ustadmobile.libuicompose.view.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewScreenForViewModel

interface ClazzAssignmentDetailOverviewFragmentEventHandler {

    fun onSubmitButtonClicked()

    fun onAddFileClicked()

    fun onAddTextClicked()

}

class ClazzAssignmentDetailOverviewFragment : UstadBaseMvvmFragment() {

    private val viewModel by ustadViewModels(::ClazzAssignmentDetailOverviewViewModel)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewLifecycleOwner.lifecycleScope.launchNavigatorCollector(viewModel)
        viewLifecycleOwner.lifecycleScope.launchAppUiStateCollector(viewModel)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {
                    ClazzAssignmentDetailOverviewScreenForViewModel(viewModel)
                }
            }
        }
    }

    companion object {

        @JvmField
        val ASSIGNMENT_STATUS_MAP = mapOf(
                CourseAssignmentSubmission.NOT_SUBMITTED to R.drawable.ic_done_white_24dp,
                CourseAssignmentSubmission.SUBMITTED to R.drawable.ic_done_white_24dp,
                CourseAssignmentSubmission.MARKED to R.drawable.ic_baseline_done_all_24
        )

        @JvmField
        val SUBMISSION_POLICY_MAP = mapOf(
            ClazzAssignment.SUBMISSION_POLICY_SUBMIT_ALL_AT_ONCE to R.drawable.ic_baseline_task_alt_24,
            ClazzAssignment.SUBMISSION_POLICY_MULTIPLE_ALLOWED to R.drawable.ic_baseline_add_task_24,
        )


    }

}