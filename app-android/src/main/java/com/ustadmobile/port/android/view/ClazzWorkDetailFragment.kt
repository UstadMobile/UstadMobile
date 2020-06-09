package com.ustadmobile.port.android.view

import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzWorkDetailBinding
import com.ustadmobile.core.controller.ClazzWorkDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter


interface ClazzWorkDetailFragmentEventHandler {

}

class ClazzWorkDetailFragment: UstadDetailFragment<ClazzWork>(), ClazzWorkDetailView,
        ClazzWorkDetailFragmentEventHandler {

    private var mBinding: FragmentClazzWorkDetailBinding? = null

    private var mPresenter: ClazzWorkDetailPresenter? = null

    private var mPager: ViewPager? = null

    private var mTabLayout: TabLayout? = null

    private var mPagerAdapter: ViewNameListFragmentPagerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzWorkDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPager = mBinding?.fragmentClazzworkDetailViewpager
        mTabLayout = mBinding?.fragmentClazzworkDetailTablayout

        mPresenter = ClazzWorkDetailPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //The fab will be managed by the underlying tabs
        fabManagementEnabled = false

        val navController = findNavController()
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

        val entityUidValue : String = arguments?.getString(UstadView.ARG_ENTITY_UID)?:"0"

        val tabs = listOf(
                //ClazzWorkEditView.VIEW_NAME + "?${UstadView.ARG_ENTITY_UID}=" +entityUidValue,
                ClazzWorkDetailOverviewView.VIEW_NAME+ "?${UstadView.ARG_ENTITY_UID}=" +entityUidValue)
        val viewNameToTitle = mapOf(
                ClazzWorkEditView.VIEW_NAME to getText(R.string.edit).toString(),
                ClazzWorkDetailOverviewView.VIEW_NAME to getText(R.string.overview).toString()
        )

        mPagerAdapter = ViewNameListFragmentPagerAdapter(childFragmentManager,
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, tabs,
                VIEW_NAME_TO_FRAGMENT_CLASS, viewNameToTitle        )

        Handler().post {
            mPager?.adapter = mPagerAdapter
            mTabLayout?.setupWithViewPager(mPager)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        mPager = null
        mPagerAdapter = null
        mTabLayout = null
    }


    override var entity: ClazzWork? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzWork = value
        }

    override var studentRole: Boolean = false
        get() = field
        set(value) {
            field = value
            if(value) {
                mBinding?.fragmentClazzWorkDetailAbl?.visibility = View.GONE
            }
        }

    override fun setEditVisible(visible: Boolean) {
        if (!visible) {
            editButtonMode = EditButtonMode.GONE
        } else {
            editButtonMode = EditButtonMode.FAB
        }
    }

    companion object{
        private val VIEW_NAME_TO_FRAGMENT_CLASS = mapOf<String, Class<out Fragment>>(
                ClazzWorkEditView.VIEW_NAME to ClazzWorkEditFragment::class.java,
                ClazzWorkDetailOverviewView.VIEW_NAME to ClazzWorkDetailOverviewFragment::class.java
        )
    }

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter
}