package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.core.viewmodel.clazzassignment.detail.ClazzAssignmentDetailViewModel
import com.ustadmobile.core.viewmodel.clazzassignment.detail.submissionstab.ClazzAssignmentDetailSubmissionsTabViewModel
import com.ustadmobile.port.android.view.clazzassignment.detailoverview.ClazzAssignmentDetailOverviewFragment
import com.ustadmobile.port.android.view.clazzassignment.submissionstab.ClazzAssignmentDetailSubmissionsTabFragment

class ClazzAssignmentDetailFragment: UstadMvvmTabsFragment(VIEWNAME_TO_FRAGMENT_MAP) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    companion object {
        val VIEWNAME_TO_FRAGMENT_MAP = mapOf<String, Class<out Fragment>>(
            ClazzAssignmentDetailOverviewView.VIEW_NAME to
                ClazzAssignmentDetailOverviewFragment::class.java,
            ClazzAssignmentDetailSubmissionsTabViewModel.DEST_NAME to
                ClazzAssignmentDetailSubmissionsTabFragment::class.java
        )
    }

}