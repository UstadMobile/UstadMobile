package com.ustadmobile.port.android.view

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
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
import java.lang.IllegalArgumentException


class CustomViewNameListFragmentPageAdapter(fm: FragmentManager, behavior: Int,
                                            val vl: List<String>,
                                            val vntfcm: Map<String, Class<out Fragment>>,
                                            val vntptm: Map<String, String>,val f: Fragment)
    : ViewNameListFragmentPagerAdapter(fm, behavior, vl, vntfcm, vntptm ) {

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> {
                return f.getText(R.string.overview).toString()
            }
            1 -> {
                return f.getText(R.string.staff).toString()
            }
            2 -> {
                return f.getText(R.string.students_literal).toString()
            }
        }
        return ""
    }
}

class SchoolDetailFragment: UstadDetailFragment<School>(), SchoolDetailView {

    private var mBinding: FragmentSchoolDetailBinding? = null

    private var mPresenter: SchoolDetailPresenter? = null

    private var mPager: ViewPager? = null

    private var mTabLayout: TabLayout? = null

    private var mPagerAdapter: CustomViewNameListFragmentPageAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSchoolDetailBinding.inflate(inflater, container,
                false).also {
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
                SchoolDetailOverviewView.VIEW_NAME + "?${UstadView.ARG_ENTITY_UID}=" +
                        "${arguments?.getString(UstadView.ARG_ENTITY_UID)?:"0"}"
                ,
                SchoolMemberListView.VIEW_NAME + "?${UstadView.ARG_SCHOOLMEMBER_FILTER_STAFF}=" +
                        "${arguments?.getString(UstadView.ARG_ENTITY_UID)?:"0"}"
                ,
                SchoolMemberListView.VIEW_NAME + "?${UstadView.ARG_SCHOOLMEMBER_FILTER_STUDENTS}=" +
                        "${arguments?.getString(UstadView.ARG_ENTITY_UID)?:"0"}"
        )
        val viewNameToTitle = mapOf(
                SchoolDetailOverviewView.VIEW_NAME to getText(R.string.overview).toString(),
                SchoolMemberListView.VIEW_NAME to getText(R.string.students).toString(),
                SchoolMemberListView.VIEW_NAME to getText(R.string.staff).toString()
        )

        mPagerAdapter = CustomViewNameListFragmentPageAdapter(childFragmentManager,
                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, tabs,
                VIEW_NAME_TO_FRAGMENT_CLASS, viewNameToTitle, this
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
        mPager = null
        mPagerAdapter = null
        mTabLayout = null
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
                        PersonListFragment::class.java,
                SchoolMemberListView.VIEW_NAME to
                        SchoolMemberListFragment::class.java
        )
    }

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

}