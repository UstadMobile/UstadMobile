package com.ustadmobile.port.android.view.clazz.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.view.ClazzLogListAttendanceView
import com.ustadmobile.core.view.ClazzMemberListView
import com.ustadmobile.core.view.CourseGroupSetListView
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.port.android.view.ClazzLogListAttendanceFragment
import com.ustadmobile.port.android.view.clazzenrolment.clazzmemberlist.ClazzMemberListFragment
import com.ustadmobile.port.android.view.CourseGroupSetListFragment
import com.ustadmobile.port.android.view.UstadMvvmTabsFragment
import com.ustadmobile.port.android.view.clazz.detailoverview.ClazzDetailOverviewFragment


class ClazzDetailFragment: UstadMvvmTabsFragment(VIEWNAME_TO_FRAGMENT_MAP) {

    private val viewModel: ClazzDetailViewModel by ustadViewModels(::ClazzDetailViewModel)

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
                ClazzDetailOverviewView.VIEW_NAME to ClazzDetailOverviewFragment::class.java,
                ClazzMemberListView.VIEW_NAME to ClazzMemberListFragment::class.java,
                ClazzLogListAttendanceView.VIEW_NAME to ClazzLogListAttendanceFragment::class.java,
                CourseGroupSetListView.VIEW_NAME to CourseGroupSetListFragment::class.java
        )

    }

}