package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.aakira.napier.Napier
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentContentEntry2DetailBinding
import com.toughra.ustadmobile.databinding.ItemEntryTranslationBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ContentEntry2DetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.view.ContentEntry2DetailView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.ContentEntryProgress
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.ContentEntryWithMostRecentContainer
import com.ustadmobile.lib.db.entities.DownloadJobItem
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


interface ContentEntryDetailFragmentEventHandler {

    fun handleOnClickOpenDownloadButton()

    fun handleOnClickDeleteButton()

    fun handleOnClickManageDownloadButton()
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

    override var locallyAvailable: Boolean = false
        set(value) {
            field = value
            mBinding?.locallyAvailable = value
        }


    private inner class PresenterViewLifecycleObserver: DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            mPresenter?.onStart()
        }

        override fun onStop(owner: LifecycleOwner) {
            mPresenter?.onStop()
        }
    }

    private var presenterLifecycleObserver: PresenterViewLifecycleObserver? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter



    override fun handleOnClickOpenDownloadButton() {
        mPresenter?.handleOnClickOpenDownloadButton()
    }


    override fun handleOnClickDeleteButton() {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm)
                .setPositiveButton(R.string.delete) { _, _ -> mPresenter?.handleOnClickDeleteButton() }
                .setNegativeButton(R.string.cancel) { dialog, _ ->  dialog.cancel() }
                .setMessage(R.string.confirm_delete_message)
                .show()
    }

    override fun handleOnClickManageDownloadButton() {
        mPresenter?.handleOnClickManageDownload()
    }

    override var availableTranslationsList: DataSource.Factory<Int, ContentEntryRelatedEntryJoinWithLanguage>? = null
        get() = field
        set(value) {
            currentLiveData?.removeObserver(availableTranslationObserver)
            val accountManager: UstadAccountManager by instance()
            val dbRepo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = TAG_REPO)
            val displayTypeRepoVal = dbRepo.contentEntryRelatedEntryJoinDao
            currentLiveData = value?.asRepositoryLiveData(displayTypeRepoVal)
            currentLiveData?.observe(this, availableTranslationObserver)
            field = value
        }


    override fun showDownloadDialog(args: Map<String, String>) {
        val systemImpl: UstadMobileSystemImpl = direct.instance()
        systemImpl.go("DownloadDialog", args, requireContext())
    }

    override var downloadJobItem: DownloadJobItem? = null
        set(value) {
            Napier.d("ContentEntryDetail: Download Status = ${downloadJobItem?.djiStatus} currentDownloadJobItemStatus = $currentDownloadJobItemStatus")
            if(field.isStatusCompleted() != value.isStatusCompleted())
                activity?.invalidateOptionsMenu()

            mBinding?.downloadJobItem = value
            if(currentDownloadJobItemStatus != value?.djiStatus) {
                when {
                    value.isStatusCompletedSuccessfully() -> {
                        mBinding?.entryDownloadOpenBtn?.visibility = View.VISIBLE
                        mBinding?.entryDetailProgress?.visibility = View.GONE
                    }

                    value.isStatusQueuedOrDownloading() -> {
                        mBinding?.entryDownloadOpenBtn?.visibility = View.GONE
                        mBinding?.entryDetailProgress?.visibility = View.VISIBLE
                    }

                    else -> {
                        mBinding?.entryDownloadOpenBtn?.visibility = View.VISIBLE
                        mBinding?.entryDetailProgress?.visibility = View.GONE
                    }
                }

                currentDownloadJobItemStatus = value?.djiStatus ?: 0
            }


            if(value != null && value.isStatusQueuedOrDownloading()) {
                mBinding?.entryDetailProgress?.statusText = value.toStatusString(
                        UstadMobileSystemImpl.instance, requireContext())
                mBinding?.entryDetailProgress?.progress = if(value.downloadLength > 0) {
                    (value.downloadedSoFar.toFloat()) / (value.downloadLength.toFloat())
                }else {
                    0f
                }
            }
            field = value
        }
    override var contentEntryProgress: ContentEntryProgress? = null
        get() = field
        set(value) {
            field = value
            mBinding?.contentEntryProgress = value
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
        mPresenter = ContentEntry2DetailPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        val flexboxLayoutManager = FlexboxLayoutManager(requireContext())
        flexboxLayoutManager.flexDirection = FlexDirection.ROW
        availableTranslationAdapter = AvailableTranslationRecyclerAdapter(this,
                mPresenter)
        mBinding?.availableTranslationView?.adapter = availableTranslationAdapter
        mBinding?.availableTranslationView?.layoutManager = flexboxLayoutManager
        fabManager?.visible = true

        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())
        presenterLifecycleObserver = PresenterViewLifecycleObserver().also {
            viewLifecycleOwner.lifecycle.addObserver(it)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_content_entry, menu)
        menu.findItem(R.id.content_entry_group_activity).isVisible = downloadJobItem.isStatusCompleted()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.content_entry_group_activity -> {
                mPresenter?.handleOnClickGroupActivityButton()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        mBinding = null
        mPresenter = null
        currentLiveData = null
        downloadJobItem = null
        mBinding?.availableTranslationView?.adapter = null
        availableTranslationAdapter = null
        availableTranslationsList = null
        entity = null
        presenterLifecycleObserver?.also {
            viewLifecycleOwner.lifecycle.removeObserver(it)
        }

        presenterLifecycleObserver = null

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