package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.core.viewmodel.clazzassignment.detail.ClazzAssignmentDetailViewModel
import com.ustadmobile.port.android.view.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewFragment

class ClazzAssignmentDetailFragment: UstadMvvmTabsFragment(VIEWNAME_TO_FRAGMENT_MAP) {

    private val viewModel: ClazzAssignmentDetailViewModel by
        ustadViewModels(::ClazzAssignmentDetailViewModel)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect {
                mTabs = it.tabs
            }
        }
    }

    companion object {
        val VIEWNAME_TO_FRAGMENT_MAP = mapOf<String, Class<out Fragment>>(
            ClazzAssignmentDetailOverviewView.VIEW_NAME to
                ClazzAssignmentDetailOverviewFragment::class.java,
            ClazzAssignmentDetailStudentProgressOverviewListView.VIEW_NAME to
                ClazzAssignmentDetailStudentProgressListOverviewFragment::class.java

        )
    }

}