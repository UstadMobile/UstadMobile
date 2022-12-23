package com.ustadmobile.port.android.view

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentContentEntry2DetailBinding
import com.toughra.ustadmobile.databinding.ItemContentJobItemProgressBinding
import com.toughra.ustadmobile.databinding.ItemEntryTranslationBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ContentEntryDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryDetailOverviewView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.*
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


interface ContentEntryDetailFragmentEventHandler {

    fun handleOnClickOpen()

    fun handleOnClickDownload()

    fun handleOnClickDeleteButton()

    fun handleOnClickManageDownloadButton()

    fun handleOnClickMarkComplete()
}

class ContentEntryDetailOverviewFragment: UstadDetailFragment<ContentEntryWithMostRecentContainer>(
), ContentEntryDetailOverviewView, ContentEntryDetailFragmentEventHandler{

    private var mBinding: FragmentContentEntry2DetailBinding? = null

    private var mPresenter: ContentEntryDetailOverviewPresenter? = null

    private var currentDownloadJobItemStatus: Int = -1

    private var currentLiveData: LiveData<PagedList<ContentEntryRelatedEntryJoinWithLanguage>>? = null

    private var availableTranslationAdapter: AvailableTranslationRecyclerAdapter? = null

    private var progressListAdapter: ContentJobItemProgressRecyclerAdapter? = null

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

    override var markCompleteVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.markCompleteVisible = value
        }


    override var contentEntryButtons: ContentEntryButtonModel?
        get() = mBinding?.contentEntryButtons
        set(value) {
            if(mBinding?.contentEntryButtons?.showOpenButton != value?.showOpenButton)
                activity?.invalidateOptionsMenu()

            mBinding?.contentEntryButtons = value
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


    override fun handleOnClickOpen() {
        mPresenter?.handleClickOpenButton()
    }

    override fun handleOnClickDownload() {
        mPresenter?.handleClickDownloadButton()
    }

    override fun handleOnClickDeleteButton() {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm)
                .setPositiveButton(R.string.delete) { _, _ -> mPresenter?.handleOnClickConfirmDelete() }
                .setNegativeButton(R.string.cancel) { dialog, _ ->  dialog.cancel() }
                .setMessage(R.string.confirm_delete_message)
                .show()
    }

    override fun handleOnClickManageDownloadButton() {
        mPresenter?.handleOnClickManageDownload()
    }

    override fun handleOnClickMarkComplete() {
        mPresenter?.handleOnClickMarkComplete()
    }

    override var availableTranslationsList: DataSource.Factory<Int, ContentEntryRelatedEntryJoinWithLanguage>? = null
        get() = field
        set(value) {
            currentLiveData?.removeObserver(availableTranslationObserver)
            val accountManager: UstadAccountManager by instance()
            val dbRepo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)
            val displayTypeRepoVal = dbRepo.contentEntryRelatedEntryJoinDao
            currentLiveData = value?.asRepositoryLiveData(displayTypeRepoVal)
            currentLiveData?.observe(this, availableTranslationObserver)
            field = value
        }


    override fun showDownloadDialog(args: Map<String, String>) {
        val systemImpl: UstadMobileSystemImpl = direct.instance()
        systemImpl.go("DownloadDialog", args, requireContext())
    }

    override var activeContentJobItems: List<ContentJobItemProgress>? = null
        set(value){
            field = value
            mBinding?.contentJobItemProgressList?.visibility = if(value != null && value.isNotEmpty())
                View.VISIBLE else View.GONE
            progressListAdapter?.submitList(value)
        }

    override var scoreProgress: ContentEntryStatementScoreProgress? = null
        get() = field
        set(value) {
            field = value
            mBinding?.scoreProgress = value
        }


    class ContentJobItemProgressRecyclerAdapter():
            ListAdapter<ContentJobItemProgress,
            ContentJobItemProgressRecyclerAdapter.ProgressViewHolder>(DIFF_CALLBACK_CONTENT_JOB_PROGRESS){


        class ProgressViewHolder(val binding: ItemContentJobItemProgressBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
            return ProgressViewHolder(ItemContentJobItemProgressBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
            val item = getItem(position)
            if (item.progressTitle != null) {
                holder.binding.entryDetailProgress.statusText = item.progressTitle.toString()
            }
            holder.binding.entryDetailProgress.progress = if (item.total > 0) {
                (item.progress.toFloat()) / (item.total.toFloat())
            } else {
                0f
            }
        }

    }



    class AvailableTranslationRecyclerAdapter(var activityEventHandler: ContentEntryDetailFragmentEventHandler?,
                                              var presenter: ContentEntryDetailOverviewPresenter?):
            ListAdapter<ContentEntryRelatedEntryJoinWithLanguage, AvailableTranslationRecyclerAdapter.TranslationViewHolder>(DIFF_CALLBACK_ENTRY_LANGUAGE_JOIN) {

        class TranslationViewHolder(val binding: ItemEntryTranslationBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslationViewHolder {
            val viewHolder = TranslationViewHolder(ItemEntryTranslationBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
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
        mPresenter = ContentEntryDetailOverviewPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner).withViewLifecycle()


        progressListAdapter = ContentJobItemProgressRecyclerAdapter()
        mBinding?.contentJobItemProgressList?.adapter = progressListAdapter
        mBinding?.contentJobItemProgressList?.layoutManager = LinearLayoutManager(requireContext())

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
        menu.findItem(R.id.content_entry_group_activity).isVisible =
            (contentEntryButtons?.showOpenButton == true)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.content_entry_group_activity -> {
                mPresenter?.handleOnClickGroupActivityButton()
                true
            }
            R.id.action_share -> {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, mPresenter?.deepLink)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        mBinding = null
        mPresenter = null
        currentLiveData = null
        mBinding?.availableTranslationView?.adapter = null
        mBinding?.contentJobItemProgressList?.adapter = null
        availableTranslationAdapter = null
        availableTranslationsList = null
        progressListAdapter = null
        entity = null
        presenterLifecycleObserver?.also {
            viewLifecycleOwner.lifecycle.removeObserver(it)
        }

        presenterLifecycleObserver = null

        super.onDestroyView()
    }



    companion object{

        val DIFF_CALLBACK_CONTENT_JOB_PROGRESS: DiffUtil.ItemCallback<ContentJobItemProgress> =
                object: DiffUtil.ItemCallback<ContentJobItemProgress>(){
                    override fun areItemsTheSame(
                        oldItem: ContentJobItemProgress,
                        newItem: ContentJobItemProgress
                    ): Boolean {
                      return oldItem.cjiUid == newItem.cjiUid
                    }

                    override fun areContentsTheSame(
                        oldItem: ContentJobItemProgress,
                        newItem: ContentJobItemProgress
                    ): Boolean {
                        return (oldItem.progress == newItem.progress
                                && oldItem.total == newItem.total
                                && oldItem.progressTitle == newItem.progressTitle)
                    }

                }


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