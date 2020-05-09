package com.ustadmobile.port.android.view

import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import com.google.android.material.tabs.TabLayout
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzDetailBinding
import com.ustadmobile.core.controller.ClazzDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.ClazzMemberListView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter


interface ClazzDetailFragmentEventHandler {

}

class ClazzDetailFragment: UstadDetailFragment<Clazz>(), ClazzDetailView, ClazzDetailFragmentEventHandler {

    private var mBinding: FragmentClazzDetailBinding? = null

    private var mPresenter: ClazzDetailPresenter? = null

    private var mPagerAdapter: ViewNameListFragmentPagerAdapter? = null

    /**
     * The fab is managed by the underlying activity, therefor this will always return null
     */
    override var mActivityWithFab: UstadListViewActivityWithFab?
        get() = null
        set(value) {}

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
                    it.fragmentClazzDetailTabs.setupWithViewPager(it.fragmentClazzDetailViewpager)
                }
            }
        }

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View

        mBinding = FragmentClazzDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentClazzDetailTabs.tabGravity = TabLayout.GRAVITY_FILL
        }

        mPresenter = ClazzDetailPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
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

    override fun onResume() {
        super.onResume()

        //TODO: Set title here
    }

    override var entity: Clazz? = null
        get() = field
        set(value) {
            field = value
            (activity as? AppCompatActivity)?.supportActionBar?.title = value?.clazzName
            mBinding?.clazz = value
        }

    companion object {
        val viewNameToFragmentMap = mapOf<String, Class<out Fragment>>(
                ClazzDetailOverviewView.VIEW_NAME to ClazzDetailOverviewFragment::class.java,
                ClazzMemberListView.VIEW_NAME to ClazzMemberListFragment::class.java)

        val viewNameToTitleMap = mapOf(
                ClazzDetailOverviewView.VIEW_NAME to R.string.overview,
                ClazzMemberListView.VIEW_NAME to R.string.members
        )

    }

}