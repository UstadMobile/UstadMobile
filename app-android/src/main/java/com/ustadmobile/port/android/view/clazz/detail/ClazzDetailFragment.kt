package com.ustadmobile.port.android.view.clazz.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.core.viewmodel.clazz.detailoverview.ClazzDetailOverviewViewModel
import com.ustadmobile.core.viewmodel.clazzenrolment.clazzmemberlist.ClazzMemberListViewModel
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceViewModel
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListViewModel
import com.ustadmobile.port.android.view.clazzlog.attendancelist.ClazzLogListAttendanceFragment
import com.ustadmobile.port.android.view.clazzenrolment.clazzmemberlist.ClazzMemberListFragment
import com.ustadmobile.port.android.view.coursegroupset.list.CourseGroupSetListFragment
import com.ustadmobile.port.android.view.UstadMvvmTabsFragment
import com.ustadmobile.port.android.view.clazz.detailoverview.ClazzDetailOverviewFragment


class ClazzDetailFragment: UstadMvvmTabsFragment(VIEWNAME_TO_FRAGMENT_MAP) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    companion object {
        val VIEWNAME_TO_FRAGMENT_MAP = mapOf<String, Class<out Fragment>>(
                ClazzDetailOverviewViewModel.DEST_NAME to ClazzDetailOverviewFragment::class.java,
                ClazzMemberListViewModel.DEST_NAME to ClazzMemberListFragment::class.java,
                ClazzLogListAttendanceViewModel.DEST_NAME to ClazzLogListAttendanceFragment::class.java,
                CourseGroupSetListViewModel.DEST_NAME to CourseGroupSetListFragment::class.java
        )

    }

}