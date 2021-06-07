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
import com.toughra.ustadmobile.databinding.FragmentClazzDetailBinding
import com.ustadmobile.core.controller.ClazzDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter
import kotlinx.android.synthetic.main.appbar_material_tabs_fixed.view.*


interface ClazzDetailFragmentEventHandler {

}

class ClazzDetailFragment: UstadDetailFragment<Clazz>(), ClazzDetailView, ClazzDetailFragmentEventHandler {

    private var mBinding: FragmentClazzDetailBinding? = null

    private var mPresenter: ClazzDetailPresenter? = null

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

                    it.fragmentClazzDetailViewpager.adapter = mPagerAdapter
                    it.root.tabs.setupWithViewPager(it.fragmentClazzDetailViewpager)
                }
            }
        }

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View

        //The fab will be managed by the underlying tabs
        fabManagementEnabled = false

        mBinding = FragmentClazzDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.root.tabs.tabGravity = TabLayout.GRAVITY_FILL
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = ClazzDetailPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        mPresenter?.onCreate(backStackSavedState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.fragmentClazzDetailViewpager?.adapter = null
        mPagerAdapter = null
        mBinding = null
        mPresenter = null
        entity = null
        tabs = null
    }

    override var entity: Clazz? = null
        get() = field
        set(value) {
            field = value
            ustadFragmentTitle = value?.clazzName
            mBinding?.clazz = value
        }

    companion object {
        val viewNameToFragmentMap = mapOf<String, Class<out Fragment>>(
                ClazzDetailOverviewView.VIEW_NAME to ClazzDetailOverviewFragment::class.java,
                ContentEntryList2View.CLAZZ_VIEW_NAME to ContentEntryList2Fragment::class.java,
                ClazzMemberListView.VIEW_NAME to ClazzMemberListFragment::class.java,
                ClazzLogListAttendanceView.VIEW_NAME to ClazzLogListAttendanceFragment::class.java,
                ClazzWorkListView.VIEW_NAME to ClazzWorkListFragment::class.java

        )

        val viewNameToTitleMap = mapOf(
                ClazzDetailOverviewView.VIEW_NAME to R.string.overview,
                ContentEntryList2View.CLAZZ_VIEW_NAME to R.string.content,
                ClazzMemberListView.VIEW_NAME to R.string.members,
                ClazzLogListAttendanceView.VIEW_NAME to R.string.attendance,
                ClazzWorkListView.VIEW_NAME to R.string.clazz_work
        )

    }

}