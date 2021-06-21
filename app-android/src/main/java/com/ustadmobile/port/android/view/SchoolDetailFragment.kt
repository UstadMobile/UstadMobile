package com.ustadmobile.port.android.view

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSchoolDetailBinding
import com.ustadmobile.core.controller.SchoolDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter


class CustomViewNameListFragmentPageAdapter(fm: FragmentManager, behavior: Int,
                val vl: List<String>, viewNameToFragmentClass: Map<String, Class<out Fragment>>,
                viewNameToPageTitle: Map<String, String>, val f: Fragment)
    : ViewNameListFragmentPagerAdapter(fm, behavior, vl, viewNameToFragmentClass, viewNameToPageTitle ) {

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> {
                return f.getText(R.string.overview).toString()
            }
            1 -> {
                return f.getText(R.string.staff).toString()
            }
            2 -> {
                return f.getText(R.string.students).toString()
            }
        }
        return ""
    }
}

class SchoolDetailFragment: UstadDetailFragment<School>(), SchoolDetailView {

    private var mBinding: FragmentSchoolDetailBinding? = null

    private var mPresenter: SchoolDetailPresenter? = null

    private var mPager: ViewPager? = null


    private var mPagerAdapter: CustomViewNameListFragmentPageAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View

        //The fab will be managed by the underlying tabs
        fabManagementEnabled = false

        mBinding = FragmentSchoolDetailBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
        }
        mPager = mBinding?.fragmentSchoolDetailViewpager

        mPresenter = SchoolDetailPresenter(requireContext(),arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val navController = findNavController()
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

        val entityUidValue : String = arguments?.getString(UstadView.ARG_ENTITY_UID)?:"0"

        val tabs = listOf(
                SchoolDetailOverviewView.VIEW_NAME + "?${UstadView.ARG_ENTITY_UID}=" +
                        entityUidValue
                ,
                SchoolMemberListView.VIEW_NAME + "?${UstadView.ARG_FILTER_BY_ROLE}=" +
                        Role.ROLE_SCHOOL_STAFF_UID +
                        "&${UstadView.ARG_FILTER_BY_SCHOOLUID}=" + entityUidValue
                ,
                SchoolMemberListView.VIEW_NAME + "?${UstadView.ARG_FILTER_BY_ROLE}=" +
                        Role.ROLE_SCHOOL_STUDENT_UID +
                        "&${UstadView.ARG_FILTER_BY_SCHOOLUID}=" + entityUidValue
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
            mBinding?.fragmentSchoolTabsFixed?.tabs?.setupWithViewPager(mPager)
        }
    }

    override var title: String? = null

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        mPager = null
        mPagerAdapter = null
    }

    override var entity: School? = null
        set(value) {
            field = value
            mBinding?.school = value
            ustadFragmentTitle = value?.schoolName
        }



    companion object{
        private val VIEW_NAME_TO_FRAGMENT_CLASS =
                mapOf<String, Class<out Fragment>>(
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