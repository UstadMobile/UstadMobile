package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.paging.DataSource
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.FragmentSiteDetailBinding
import com.toughra.ustadmobile.databinding.ItemSiteBinding
import com.ustadmobile.core.controller.SiteDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SiteDetailView
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTerms


interface WorkspaceDetailFragmentEventHandler {

}

class SiteDetailFragment: UstadDetailFragment<Site>(), SiteDetailView, WorkspaceDetailFragmentEventHandler {

    class SiteViewHolder(val mBinding: ItemSiteBinding): RecyclerView.ViewHolder(mBinding.root)

    class SiteRecyclerViewAdapter : ListAdapter<Site, SiteViewHolder>(DIFFUTIL_SITE) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteViewHolder {
            return SiteViewHolder(ItemSiteBinding.inflate(LayoutInflater.from(parent.context),
                parent, false))
        }

        override fun onBindViewHolder(holder: SiteViewHolder, position: Int) {
            holder.mBinding.site = getItem(position)
        }
    }

    private var mBinding: FragmentSiteDetailBinding? = null

    private var mPresenter: SiteDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var siteRecyclerViewAdapter: SiteRecyclerViewAdapter? = null


    override var entity: Site? = null
        set(value) {
            field = value

            siteRecyclerViewAdapter?.submitList(if(value != null) {
                listOf(value)
            }else {
                listOf()
            })
        }


    override var siteTermsList: DataSource.Factory<Int, SiteTerms>? = null
        set(value) {
            field = value

        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView: View
        mBinding = FragmentSiteDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        siteRecyclerViewAdapter = SiteRecyclerViewAdapter()
        mBinding?.fragmentListRecyclerview?.apply {
            adapter = siteRecyclerViewAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        mPresenter = SiteDetailPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di)
        mPresenter?.onCreate(backStackSavedState)

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }


    companion object {
        val DIFFUTIL_SITE = object: DiffUtil.ItemCallback<Site>() {
            override fun areItemsTheSame(oldItem: Site, newItem: Site): Boolean {
                return oldItem.siteUid == newItem.siteUid
            }

            override fun areContentsTheSame(oldItem: Site, newItem: Site): Boolean {
                return oldItem.siteName == newItem.siteName &&
                        oldItem.registrationAllowed == newItem.registrationAllowed &&
                        oldItem.guestLogin == newItem.guestLogin
            }
        }
    }

}