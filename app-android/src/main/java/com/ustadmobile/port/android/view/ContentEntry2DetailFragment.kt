package com.ustadmobile.port.android.view

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentContentEntry2DetailBinding
import com.toughra.ustadmobile.databinding.ItemEntryTranslationBinding
import com.ustadmobile.core.controller.ContentEntry2DetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.util.goToContentEntry
import com.ustadmobile.core.view.ContentEntry2DetailView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.ContentEntryWithMostRecentContainer
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.port.android.view.ext.runAfterRequestingPermissionIfNeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


interface ContentEntryDetailFragmentEventHandler {

    fun handleOnClickOpenDownloadButton()
}

class ContentEntry2DetailFragment: UstadDetailFragment<ContentEntryWithMostRecentContainer>(), ContentEntry2DetailView, ContentEntryDetailFragmentEventHandler{

    private var mBinding: FragmentContentEntry2DetailBinding? = null

    private var mPresenter: ContentEntry2DetailPresenter? = null

    private var currentDownloadJobItemStatus: Int = -1

    private var currentLiveData: LiveData<PagedList<ContentEntryRelatedEntryJoinWithLanguage>>? = null

    private var availableTranslationAdapter: AvailableTranslationRecyclerAdapter? = null

    private val availableTranslationObserver = Observer<List<ContentEntryRelatedEntryJoinWithLanguage>?> {
        t -> run {
            mBinding?.translationVisibility = if (t != null && t.isNotEmpty()) View.VISIBLE else View.GONE
            availableTranslationAdapter?.submitList(t)
        }
    }


    override var entity: ContentEntryWithMostRecentContainer? = null
        get() = field
        set(value) {
            field = value
            mBinding?.contentEntry = value
        }


    override var editButtonMode: EditButtonMode = EditButtonMode.GONE
        get() = field
        set(value) {
            mBinding?.editButtonMode = value
            field = value
        }
    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter



    override fun handleOnClickOpenDownloadButton() {
        mPresenter?.handleOnClickOpenDownloadButton()
    }

    override var availableTranslationsList: DataSource.Factory<Int, ContentEntryRelatedEntryJoinWithLanguage>? = null
        get() = field
        set(value) {
            currentLiveData?.removeObserver(availableTranslationObserver)
            val dbRepo = UmAccountManager.getRepositoryForActiveAccount(requireContext())
            val displayTypeRepoVal = dbRepo.contentEntryRelatedEntryJoinDao
            currentLiveData = value?.asRepositoryLiveData(displayTypeRepoVal)
            currentLiveData?.observe(this, availableTranslationObserver)
            field = value
        }


    override var downloadOptions: Map<String, String>? = null
        set(value) {
            if(value != null){
                runAfterRequestingPermissionIfNeeded(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    UstadMobileSystemImpl.instance.go("DownloadDialog", value, requireContext())
                }
            }

        }
    override var downloadJobItem: DownloadJobItem? = null
        set(value) {
            if(currentDownloadJobItemStatus != value?.djiStatus) {
                when {
                    value.isStatusCompletedSuccessfully() -> {
                        mBinding?.entryDownloadOpenBtn?.visibility = View.VISIBLE
                        mBinding?.entryDownloadOpenBtn?.text = resources.getText(R.string.open)
                        mBinding?.entryDetailProgress?.visibility = View.GONE
                    }

                    value.isStatusQueuedOrDownloading() -> {
                        mBinding?.entryDownloadOpenBtn?.visibility = View.GONE
                        mBinding?.entryDetailProgress?.visibility = View.VISIBLE
                    }

                    else -> {
                        mBinding?.entryDownloadOpenBtn?.text = resources.getText(R.string.download)
                        mBinding?.entryDownloadOpenBtn?.visibility = View.VISIBLE
                        mBinding?.entryDetailProgress?.visibility = View.GONE
                    }
                }

                currentDownloadJobItemStatus = value?.djiStatus ?: 0
            }


            if(value != null && value.isStatusQueuedOrDownloading()) {
                mBinding?.entryDetailProgress?.statusText = value.toStatusString(
                        UstadMobileSystemImpl.instance, this)
                mBinding?.entryDetailProgress?.progress = if(value.downloadLength > 0) {
                    (value.downloadedSoFar.toFloat()) / (value.downloadLength.toFloat())
                }else {
                    0f
                }
            }
            field = value
        }

    class AvailableTranslationRecyclerAdapter(var activityEventHandler: ContentEntryDetailFragmentEventHandler?,
                                              var presenter: ContentEntry2DetailPresenter?):
            ListAdapter<ContentEntryRelatedEntryJoinWithLanguage, AvailableTranslationRecyclerAdapter.TranslationViewHolder>(DIFF_CALLBACK_ENTRY_LANGUAGE_JOIN) {

        class TranslationViewHolder(val binding: ItemEntryTranslationBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslationViewHolder {
            val viewHolder = TranslationViewHolder(ItemEntryTranslationBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.mActivity = activityEventHandler
            return viewHolder
        }

        override fun onBindViewHolder(holder: TranslationViewHolder, position: Int) {
            holder.binding.entryWithLanguage = getItem(position)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
            activityEventHandler = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentContentEntry2DetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentEventHandler = this
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GlobalScope.launch {
            val networkManagerBle = (activity as? MainActivity)?.networkManagerBle?.await()
            val thisFrag = this@ContentEntry2DetailFragment
            withContext(Dispatchers.Main){
                mPresenter = ContentEntry2DetailPresenter(requireContext(), arguments.toStringMap(), thisFrag,
                        thisFrag.viewLifecycleOwner, UstadMobileSystemImpl.instance, true,
                        UmAccountManager.getActiveDatabase(requireContext()),
                        UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                        networkManagerBle?.containerDownloadManager,
                        UmAccountManager.activeAccountLiveData, ::goToContentEntry)
                mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

                val flexboxLayoutManager = FlexboxLayoutManager(requireContext())
                flexboxLayoutManager.flexDirection = FlexDirection.ROW
                availableTranslationAdapter = AvailableTranslationRecyclerAdapter(thisFrag,mPresenter)
                mBinding?.availableTranslationView?.adapter = availableTranslationAdapter
                mBinding?.availableTranslationView?.layoutManager = flexboxLayoutManager
                fabManager?.visible = true
            }
        }
    }

    override fun onDestroyView() {
        mBinding = null
        mPresenter = null
        downloadOptions = null
        currentLiveData = null
        downloadJobItem = null
        mBinding?.availableTranslationView?.adapter = null
        availableTranslationAdapter = null
        availableTranslationsList = null
        entity = null
        super.onDestroyView()
    }



    companion object{

        val DIFF_CALLBACK_ENTRY_LANGUAGE_JOIN: DiffUtil.ItemCallback<ContentEntryRelatedEntryJoinWithLanguage> =
                object: DiffUtil.ItemCallback<ContentEntryRelatedEntryJoinWithLanguage>() {
            override fun areItemsTheSame(oldItem: ContentEntryRelatedEntryJoinWithLanguage,
                                         newItem: ContentEntryRelatedEntryJoinWithLanguage): Boolean {
                return oldItem.cerejRelatedEntryUid == newItem.cerejRelatedEntryUid
            }

            override fun areContentsTheSame(oldItem: ContentEntryRelatedEntryJoinWithLanguage,
                                            newItem: ContentEntryRelatedEntryJoinWithLanguage): Boolean {
                return oldItem == newItem
            }
        }
    }
}