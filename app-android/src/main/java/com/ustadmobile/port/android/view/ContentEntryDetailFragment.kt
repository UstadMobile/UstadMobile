package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentContentEntryDetailViewpagerBinding
import com.ustadmobile.core.controller.ContentEntryDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryDetailAttemptsListView
import com.ustadmobile.core.view.ContentEntryDetailOverviewView
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.android.view.ext.createTabLayoutStrategy
import com.ustadmobile.port.android.view.util.ForeignKeyAttachmentUriAdapter
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter


class ContentEntryDetailFragment: UstadDetailFragment<ContentEntry>(), ContentEntryDetailView {

    private var mBinding: FragmentContentEntryDetailViewpagerBinding? = null

    private var mPresenter: ContentEntryDetailPresenter? = null

    private var mPagerAdapter: ViewNameListFragmentPagerAdapter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter


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
            mPagerAdapter = ViewNameListFragmentPagerAdapter(childFragmentManager,
                    lifecycle, value, viewNameToFragmentMap)

            val pager = mBinding?.fragmentContentEntryDetailViewpager ?: return
            val tabList = mBinding?.fragmentContentEntryTabs?.tabs ?: return

            pager.adapter = mPagerAdapter

            mediator = TabLayoutMediator(tabList, pager, viewNameToTitleMap.createTabLayoutStrategy(value, requireContext()))
            mediator?.attach()
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View

        //The fab will be managed by the underlying tabs
        fabManagementEnabled = false

        mBinding = FragmentContentEntryDetailViewpagerBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentContentEntryTabs.tabs.tabGravity = TabLayout.GRAVITY_FILL
        }

        mPresenter = ContentEntryDetailPresenter(requireContext(), arguments.toStringMap(),
                this, di, viewLifecycleOwner).withViewLifecycle()

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
        mBinding?.fragmentContentEntryDetailViewpager?.adapter = null
        mPagerAdapter = null
        mBinding = null
        mPresenter = null
        entity = null
        tabs = null
    }

    override var entity: ContentEntry? = null
        get() = field
        set(value) {
            field = value
            ustadFragmentTitle = value?.title
            mBinding?.entry = value
        }

    companion object {
        val viewNameToFragmentMap = mapOf<String, Class<out Fragment>>(
                ContentEntryDetailOverviewView.VIEW_NAME to ContentEntryDetailOverviewFragment::class.java,
                ContentEntryDetailAttemptsListView.VIEW_NAME to ContentEntryDetailAttemptsListFragment::class.java
        )

        val viewNameToTitleMap = mapOf(
                ContentEntryDetailOverviewView.VIEW_NAME to R.string.overview,
                ContentEntryDetailAttemptsListView.VIEW_NAME to R.string.attempts
        )

        @JvmStatic
        val FOREIGNKEYADAPTER_ENTRY = object: ForeignKeyAttachmentUriAdapter {
            override suspend fun getAttachmentUri(foreignKey: Long, dbToUse: UmAppDatabase): String? {
                return dbToUse.contentEntryPictureDao.findByContentEntryUidAsync(foreignKey)?.cepUri
            }
        }

    }



}