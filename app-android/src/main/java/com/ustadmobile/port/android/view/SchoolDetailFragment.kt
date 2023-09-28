package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSchoolDetailBinding
import com.ustadmobile.core.controller.SchoolDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.*
import com.ustadmobile.core.viewmodel.SchoolDetailOverviewViewModel
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter
import com.ustadmobile.core.R as CR

class SchoolDetailFragment: UstadDetailFragment<School>(), SchoolDetailView {

    private var mBinding: FragmentSchoolDetailBinding? = null

    private var mPresenter: SchoolDetailPresenter? = null

    private var mPager: ViewPager2? = null

    private var mTabs: TabLayout? = null

    private var mediator: TabLayoutMediator? = null

    private var mPagerAdapter: ViewNameListFragmentPagerAdapter? = null

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
        mTabs = mBinding?.fragmentSchoolTabsFixed?.tabs

        mPresenter = SchoolDetailPresenter(requireContext(),arguments.toStringMap(), this,
                di, viewLifecycleOwner).withViewLifecycle()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val navController = findNavController()
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

        val entityUidValue : String = arguments?.getString(UstadView.ARG_ENTITY_UID)?:"0"
        val commonArgs = mapOf(UstadView.ARG_NAV_CHILD to true.toString())

        val tabs = listOf(
            SchoolDetailOverviewViewModel.DEST_NAME.appendQueryArgs(
                commonArgs + mapOf(UstadView.ARG_ENTITY_UID to entityUidValue)
            ),
        )

        mPagerAdapter = ViewNameListFragmentPagerAdapter(childFragmentManager, lifecycle,
                tabs, VIEW_NAME_TO_FRAGMENT_CLASS)
        mPager?.adapter = mPagerAdapter

        val pager = mPager ?: return
        val tabList = mTabs ?: return

        mediator = TabLayoutMediator(tabList, pager) { tab, position ->
            tab.text = when (position) {
                0 -> {
                    getText(CR.string.overview).toString()
                }
                1 -> {
                    getText(CR.string.staff).toString()
                }
                2 -> {
                    getText(CR.string.students).toString()
                }
                else -> ""
            }
        }
        mediator?.attach()
    }

    override var title: String? = null

    override fun onDestroyView() {
        super.onDestroyView()
        mediator?.detach()
        mediator = null
        mBinding?.fragmentSchoolDetailViewpager?.adapter = null
        mBinding = null
        mPresenter = null
        entity = null
        mPager = null
        mTabs = null
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
                SchoolDetailOverviewViewModel.DEST_NAME to
                        SchoolDetailOverviewFragment::class.java,
                PersonListViewModel.DEST_NAME to
                        PersonListFragment::class.java,
        )
    }

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

}