package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSchoolDetailBinding
import com.ustadmobile.core.controller.SchoolDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SchoolDetailView
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.core.view.EditButtonMode


class SchoolDetailFragment: UstadDetailFragment<School>(), SchoolDetailView {

    private var mBinding: FragmentSchoolDetailBinding? = null

    private var mPresenter: SchoolDetailPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSchoolDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = SchoolDetailPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

//    /**
//     * Separated out view pager setup for clarity.
//     */
//    private fun setupViewPager(tabs:List<String>) {
//
//        val viewNameToTitle = mapOf(PersoListView.VIEW_NAME to getText(R.string.students).toString()
//        )
//
////                ClazzAssignmentDetailAssignmentView.VIEW_NAME to getText(R.string.assignments).toString(),
////                ClazzAssignmentDetailProgressView.VIEW_NAME to getText(R.string.student_progress).toString()
//
//        runOnUiThread {
//            mPager = rootView?.clazzAssignmentDetailViewPagerContainer
//            mPagerAdapter = ViewNameListFragmentPagerAdapter(supportFragmentManager,
//                    FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, tabs,
//                    VIEW_NAME_TO_FRAGMENT_CLASS, viewNameToTitle,bundleMakerFn
//            )
//
//            mPager?.adapter = mPagerAdapter
//            mTabLayout = rootView?.activityClazzAssignmentDetailTablayout
//            mTabLayout?.tabGravity = TabLayout.GRAVITY_FILL
//            mTabLayout?.setupWithViewPager(mPager)
//        }
//    }

    override fun setUpTabs(tabs: List<String>){
        //TODO: this
    }

    override fun setSettingsVisible(visible: Boolean){
        //TODO: this
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()

        //TODO: Set title here
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
//        private val VIEW_NAME_TO_FRAGMENT_CLASS = mapOf<String, Class<out Fragment>>(
//
////                ClazzAssignmentDetailAssignmentView.VIEW_NAME to
////                        ClazzAssignmentDetailAssignmentFragment::class.java,
////                ClazzAssignmentDetailProgressView.VIEW_NAME to
////                        ClazzAssignmentDetailProgressFragment::class.java)
//        )
    }

}