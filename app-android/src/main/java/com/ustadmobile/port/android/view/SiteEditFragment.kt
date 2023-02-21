package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.FragmentSiteEditBinding
import com.toughra.ustadmobile.databinding.ItemSiteTermsEditBinding
import com.ustadmobile.core.controller.SiteEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SiteEditView
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage


class SiteEditFragment: UstadEditFragment<Site>(), SiteEditView {

    private var mBinding: FragmentSiteEditBinding? = null

    private var mPresenter: SiteEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, Site>?
        get() = mPresenter


    class SiteTermsRecyclerAdapter(var presenter: SiteEditPresenter?): ListAdapter<SiteTermsWithLanguage, SiteTermsRecyclerAdapter.SiteTermsViewHolder>(DIFF_CALLBACK_WORKSPACETERMS) {

            class SiteTermsViewHolder(val binding: ItemSiteTermsEditBinding): RecyclerView.ViewHolder(binding.root)

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteTermsViewHolder {
                val viewHolder = SiteTermsViewHolder(ItemSiteTermsEditBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false))
                viewHolder.binding.mPresenter = presenter
                return viewHolder
            }

            override fun onBindViewHolder(holder: SiteTermsViewHolder, position: Int) {
                holder.binding.siteTerms = getItem(position)
            }
        }

    override var siteTermsList: LiveData<List<SiteTermsWithLanguage>>? = null
        get() = field
        set(value) {
            field?.removeObserver(workspaceTermsObserver)
            field = value
            value?.observe(this, workspaceTermsObserver)
        }

    private var siteTermsRecyclerAdapter: SiteTermsRecyclerAdapter? = null

    //private var workspaceTermsRecyclerView: RecyclerView? = null

    private val workspaceTermsObserver = Observer<List<SiteTermsWithLanguage>?> {
        t -> siteTermsRecyclerAdapter?.submitList(t)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSiteEditBinding.inflate(inflater, container, false).also {
            rootView = it.root

            siteTermsRecyclerAdapter = SiteTermsRecyclerAdapter(mPresenter)
            it.siteTermsRv.adapter = siteTermsRecyclerAdapter
            it.siteTermsRv.layoutManager = LinearLayoutManager(requireContext())
            it.mPresenter = mPresenter
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = SiteEditPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di).withViewLifecycle()
        siteTermsRecyclerAdapter?.presenter = mPresenter

        mPresenter?.onCreate(backStackSavedState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()
    }

    override var entity: Site? = null
        get() = field
        set(value) {
            field = value
            mBinding?.site = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }

    companion object {

        val DIFF_CALLBACK_WORKSPACETERMS = object: DiffUtil.ItemCallback<SiteTermsWithLanguage>() {
            override fun areItemsTheSame(oldItem: SiteTermsWithLanguage, newItem: SiteTermsWithLanguage): Boolean {
                return oldItem.sTermsUid == newItem.sTermsUid
            }

            override fun areContentsTheSame(oldItem: SiteTermsWithLanguage, newItem: SiteTermsWithLanguage): Boolean {
                return oldItem.sTermsLang == newItem.sTermsLang
                        && oldItem.termsHtml == newItem.termsHtml
            }
        }

    }

}