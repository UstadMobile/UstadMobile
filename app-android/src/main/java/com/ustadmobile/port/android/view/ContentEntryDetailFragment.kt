package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.toughra.ustadmobile.databinding.FragmentContentEntryDetailViewpagerBinding
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.viewmodel.contententry.detailoverviewtab.ContentEntryDetailOverviewViewModel
import com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab.ContentEntryDetailAttemptListViewModel
import com.ustadmobile.port.android.view.util.ForeignKeyAttachmentUriAdapter
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter
import com.ustadmobile.core.R as CR


class ContentEntryDetailFragment: UstadBaseMvvmFragment() {

    private var mBinding: FragmentContentEntryDetailViewpagerBinding? = null


    private var mPagerAdapter: ViewNameListFragmentPagerAdapter? = null

    private var mediator: TabLayoutMediator? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View

        //The fab will be managed by the underlying tabs
        fabManagementEnabled = false

        mBinding = FragmentContentEntryDetailViewpagerBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentContentEntryTabs.tabs.tabGravity = TabLayout.GRAVITY_FILL
        }


        return rootView
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mediator?.detach()
        mediator = null
        mBinding?.fragmentContentEntryDetailViewpager?.adapter = null
        mPagerAdapter = null
        mBinding = null
        entity = null
    }

    companion object {
        val viewNameToFragmentMap = mapOf<String, Class<out Fragment>>(
                ContentEntryDetailOverviewViewModel.DEST_NAME to ContentEntryDetailOverviewFragment::class.java,
                ContentEntryDetailAttemptListViewModel.DEST_NAME to ContentEntryDetailAttemptsListFragment::class.java
        )

        val viewNameToTitleMap = mapOf(
                ContentEntryDetailOverviewViewModel.DEST_NAME to CR.string.overview,
            ContentEntryDetailAttemptListViewModel.DEST_NAME to CR.string.attempts
        )

        @JvmStatic
        val FOREIGNKEYADAPTER_ENTRY = object: ForeignKeyAttachmentUriAdapter {
            override suspend fun getAttachmentUri(foreignKey: Long, dbToUse: UmAppDatabase): String? {
                return dbToUse.contentEntryPictureDao.findByContentEntryUidAsync(foreignKey)?.cepUri
            }
        }

    }



}