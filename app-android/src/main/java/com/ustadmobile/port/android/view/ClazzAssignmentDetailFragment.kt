package com.ustadmobile.port.android.view

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import com.google.android.material.tabs.TabLayout
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzAssignmentDetailBinding
import com.ustadmobile.core.controller.ClazzAssignmentDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter
import kotlinx.android.synthetic.main.appbar_material_tabs_fixed.view.*


interface ClazzAssignmentDetailFragmentEventHandler {

}

class ClazzAssignmentDetailFragment: UstadDetailFragment<ClazzAssignment>(), ClazzAssignmentDetailView, ClazzDetailFragmentEventHandler {

    private var mBinding: FragmentClazzAssignmentDetailBinding? = null

    private var mPresenter: ClazzAssignmentDetailPresenter? = null

    private var mPagerAdapter: ViewNameListFragmentPagerAdapter? = null

    override var tabs: List<String>? = null
        get() = field
        set(value) {
            if(field == value)
                return

            field = value

            if(value == null)
                return

            field = value
            mPagerAdapter = ViewNameListFragmentPagerAdapter(childFragmentManager,
                    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, value, viewNameToFragmentMap,
                    viewNameToTitleMap.map { it.key to requireContext().getString(it.value) }.toMap())
            Handler().post {
                mBinding.also {
                    if(it == null)
                        return@also

                    it.fragmentClazzAssignmentDetailViewpager.adapter = mPagerAdapter
                    it.root.tabs.setupWithViewPager(it.fragmentClazzAssignmentDetailViewpager)
                }
            }
        }

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View

        //The fab will be managed by the underlying tabs
        fabManagementEnabled = false

        mBinding = FragmentClazzAssignmentDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.root.tabs.tabGravity = TabLayout.GRAVITY_FILL
        }

        mPresenter = ClazzAssignmentDetailPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.fragmentClazzAssignmentDetailViewpager?.adapter = null
        mPagerAdapter = null
        mBinding = null
        mPresenter = null
        entity = null
        tabs = null
    }

    override var entity: ClazzAssignment? = null
        get() = field
        set(value) {
            field = value
            ustadFragmentTitle = value?.caTitle
            mBinding?.clazzAssignment = value
        }

    companion object {
        val viewNameToFragmentMap = mapOf<String, Class<out Fragment>>(
                ClazzAssignmentDetailOverviewView.VIEW_NAME to
                        ClazzAssignmentDetailOverviewFragment::class.java,
                ClazzAssignmentDetailStudentProgressOverviewListView.VIEW_NAME to
                        ClazzAssignmentDetailStudentProgressListOverviewFragment::class.java

        )

        val viewNameToTitleMap = mapOf(
                ClazzAssignmentDetailOverviewView.VIEW_NAME to R.string.overview,
                ClazzAssignmentDetailStudentProgressOverviewListView.VIEW_NAME to R.string.student_progress,
        )

    }

}