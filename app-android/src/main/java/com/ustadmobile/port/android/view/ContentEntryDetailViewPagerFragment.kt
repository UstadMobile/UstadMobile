package com.ustadmobile.port.android.view

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentContentEntryDetailViewpagerBinding
import com.ustadmobile.core.controller.ContentEntryDetailViewPagerPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter
import kotlinx.android.synthetic.main.appbar_material_tabs_fixed.view.*


class ContentEntryDetailViewPagerFragment: UstadDetailFragment<ContentEntry>(), ContentEntryDetailViewPagerView {

    private var mBinding: FragmentContentEntryDetailViewpagerBinding? = null

    private var mPresenter: ContentEntryDetailViewPagerPresenter? = null

    private var mPagerAdapter: ViewNameListFragmentPagerAdapter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter


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
                    FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, value, viewNameToFragmentMap,
                    viewNameToTitleMap.map { it.key to requireContext().getString(it.value) }.toMap())
            Handler().post {
                mBinding.also {
                    if(it == null)
                        return@also

                    it.fragmentContentEntryDetailViewpager.adapter = mPagerAdapter
                    it.root.tabs.setupWithViewPager(it.fragmentContentEntryDetailViewpager)
                }
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View

        //The fab will be managed by the underlying tabs
        fabManagementEnabled = false

        mBinding = FragmentContentEntryDetailViewpagerBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.root.tabs.tabGravity = TabLayout.GRAVITY_FILL
        }

        mPresenter = ContentEntryDetailViewPagerPresenter(requireContext(), arguments.toStringMap(),
                this, di, viewLifecycleOwner)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
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

    }



}