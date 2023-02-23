package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import com.google.android.material.composethemeadapter.MdcTheme
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzAssignmentDetailBinding
import com.ustadmobile.core.controller.ClazzAssignmentDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.viewmodel.ClazzAssignmentDetailUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.defaultScreenPadding
import com.ustadmobile.port.android.view.ext.createTabLayoutStrategy
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter


interface ClazzAssignmentDetailFragmentEventHandler {

}

class ClazzAssignmentDetailFragment: UstadDetailFragment<ClazzAssignment>(), ClazzAssignmentDetailView, ClazzDetailFragmentEventHandler {

    private var mBinding: FragmentClazzAssignmentDetailBinding? = null

    private var mPresenter: ClazzAssignmentDetailPresenter? = null

    private var mPagerAdapter: ViewNameListFragmentPagerAdapter? = null

    private var mediator: TabLayoutMediator? = null

    override var tabs: List<String>? = null
        get() = field
        set(value) {
            if(field == value)
                return

            field = value

            if(value == null)
                return

            field = value
            mPagerAdapter = ViewNameListFragmentPagerAdapter(childFragmentManager, lifecycle,
                    value, VIEWNAME_TO_FRAGMENT_MAP)

            val pager = mBinding?.fragmentClazzAssignmentDetailViewpager ?: return
            val tabList = mBinding?.fragmentClazzAssignmentDetailTabs?.tabs ?: return

            pager.adapter = mPagerAdapter

            mediator = TabLayoutMediator(tabList, pager, VIEWNAME_TO_TITLE_MAP.createTabLayoutStrategy(value, requireContext()))
            mediator?.attach()
        }

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View

        //The fab will be managed by the underlying tabs
        fabManagementEnabled = false

        mBinding = FragmentClazzAssignmentDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentClazzAssignmentDetailTabs.tabs.tabGravity = TabLayout.GRAVITY_FILL
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
        mediator?.detach()
        mediator = null
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
        val VIEWNAME_TO_FRAGMENT_MAP = mapOf<String, Class<out Fragment>>(
                ClazzAssignmentDetailOverviewView.VIEW_NAME to
                        ClazzAssignmentDetailOverviewFragment::class.java,
                ClazzAssignmentDetailStudentProgressOverviewListView.VIEW_NAME to
                        ClazzAssignmentDetailStudentProgressListOverviewFragment::class.java

        )

        val VIEWNAME_TO_TITLE_MAP = mapOf(
                ClazzAssignmentDetailOverviewView.VIEW_NAME to R.string.overview,
                ClazzAssignmentDetailStudentProgressOverviewListView.VIEW_NAME to R.string.submissions,
        )

    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ClazzAssignmentDetailScreen(
    uiState: ClazzAssignmentDetailUiState = ClazzAssignmentDetailUiState(),
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .defaultScreenPadding()
    ) {

    }
}

@Composable
@Preview
fun ClazzAssignmentDetailScreenPreview() {
    val uiStateVal = ClazzAssignmentDetailUiState()

    MdcTheme {
        ClazzAssignmentDetailScreen(uiStateVal)
    }
}