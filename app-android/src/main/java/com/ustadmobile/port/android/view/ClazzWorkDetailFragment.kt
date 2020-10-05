package com.ustadmobile.port.android.view

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzWorkDetailBinding
import com.ustadmobile.core.controller.ClazzWorkDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter
import kotlinx.android.synthetic.main.appbar_material_tabs_scrollable.view.*

class ClazzWorkDetailFragment: UstadDetailFragment<ClazzWork>(), ClazzWorkDetailView{

    private var mBinding: FragmentClazzWorkDetailBinding? = null

    private var mPresenter: ClazzWorkDetailPresenter? = null

    private var mPager: ViewPager? = null

    private var mTabLayout: TabLayout? = null

    private var mPagerAdapter: ViewNameListFragmentPagerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View

        mBinding = FragmentClazzWorkDetailBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
            mTabLayout = it.root.tabs
        }

        mPager = mBinding?.fragmentClazzworkDetailViewpager

        mPresenter = ClazzWorkDetailPresenter(requireContext(), arguments.toStringMap(), this,
                di, this)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //The fab will be managed by the underlying tabs
        fabManagementEnabled = false

        val navController = findNavController()
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

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

    override var isStudent: Boolean = true
        get() = field
        set(value) {
            field = value
            if(value) {
                mBinding?.root?.tabs?.visibility = View.GONE
            }else{
                mBinding?.root?.tabs?.visibility = View.VISIBLE
            }

            val entityUidValue : String = arguments?.getString(UstadView.ARG_ENTITY_UID)?:"0"

            val tabs: List<String>
            tabs = if(isStudent){
                listOf(
                        ClazzWorkDetailOverviewView.VIEW_NAME+ "?${UstadView.ARG_ENTITY_UID}=" +
                                entityUidValue
                )
            }else {
                listOf(
                        ClazzWorkDetailOverviewView.VIEW_NAME + "?${UstadView.ARG_ENTITY_UID}=" +
                                entityUidValue,
                        ClazzWorkDetailProgressListView.VIEW_NAME + "?${UstadView.ARG_ENTITY_UID}=" +
                                entityUidValue)
            }
            val viewNameToTitle = mapOf(
                    ClazzWorkEditView.VIEW_NAME to getText(R.string.edit).toString(),
                    ClazzWorkDetailOverviewView.VIEW_NAME to getText(R.string.overview).toString(),
                    ClazzWorkDetailProgressListView.VIEW_NAME to getText(R.string.student_progress).toString()
            )

            mPagerAdapter = ViewNameListFragmentPagerAdapter(childFragmentManager,
                    FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, tabs,
                    VIEW_NAME_TO_FRAGMENT_CLASS, viewNameToTitle)

            Handler().post {
                mPager?.adapter = mPagerAdapter
                mTabLayout?.setupWithViewPager(mPager)
            }
        }

    companion object{
        private val VIEW_NAME_TO_FRAGMENT_CLASS = mapOf<String,
                Class<out Fragment>>(
                ClazzWorkEditView.VIEW_NAME to ClazzWorkEditFragment::class.java,
                ClazzWorkDetailOverviewView.VIEW_NAME to
                        ClazzWorkDetailOverviewFragment::class.java,
                ClazzWorkDetailProgressListView.VIEW_NAME to
                        ClazzWorkDetailProgressListFragment::class.java
        )
    }

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter
}