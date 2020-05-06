package com.ustadmobile.port.android.view

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSchoolDetailBinding
import com.ustadmobile.core.controller.SchoolDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.setEditFragmentTitle
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter


class SchoolDetailFragment: UstadDetailFragment<School>(), SchoolDetailView {

    private var mBinding: FragmentSchoolDetailBinding? = null

    private var mPresenter: SchoolDetailPresenter? = null

    private var mPager: ViewPager? = null

    private var mTabLayout: TabLayout? = null

    private lateinit var mPagerAdapter: ViewNameListFragmentPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSchoolDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }
        mPager = mBinding?.fragmentSchoolDetailViewpager
        mTabLayout = mBinding?.fragmentSchoolDetailTablayout

        mPresenter = SchoolDetailPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

        val tabs = listOf(
                SchoolDetailOverviewView.VIEW_NAME +
                        "?${UstadView.ARG_ENTITY_UID}=${arguments?.getString(UstadView.ARG_ENTITY_UID)?:"0"}",
                PersonListView.VIEW_NAME)
        val viewNameToTitle = mapOf(
                SchoolDetailOverviewView.VIEW_NAME to getText(R.string.overview).toString(),
                PersonListView.VIEW_NAME to getText(R.string.people).toString()
        )

        mPagerAdapter = ViewNameListFragmentPagerAdapter(childFragmentManager,
                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, tabs,
                VIEW_NAME_TO_FRAGMENT_CLASS, viewNameToTitle
        )

        Handler().post {
            mPager?.adapter = mPagerAdapter
            mTabLayout?.setupWithViewPager(mPager)
        }
    }


    override fun setSettingsVisible(visible: Boolean){

        if(!visible){
            editButtonMode = EditButtonMode.GONE
        }else{
            editButtonMode = EditButtonMode.FAB
        }
    }

    override var title: String? = null
        get() = field
        set(value) {
            field = value
            (activity as? AppCompatActivity)?.supportActionBar?.title = title
        }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.school)
    }

    override var entity: School? = null
        get() = field
        set(value) {
            field = value
            mBinding?.school = value
        }

    override var editButtonMode: EditButtonMode = EditButtonMode.GONE
        get() = field
        set(value) {
            mBinding?.editButtonMode = value
            field = value
        }


    companion object{
        private val VIEW_NAME_TO_FRAGMENT_CLASS = mapOf<String, Class<out Fragment>>(
                SchoolDetailOverviewView.VIEW_NAME to
                        SchoolDetailOverviewFragment::class.java,
                PersonListView.VIEW_NAME to
                        PersonListFragment::class.java
        )
    }

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

}