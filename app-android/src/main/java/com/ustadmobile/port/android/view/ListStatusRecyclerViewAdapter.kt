package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemListStatusBinding
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.door.RepositoryLoadHelper
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADED_NODATA
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADED_WITHDATA

/**
 * This RecyclerViewAdapter is intended to be placed in a MergeAdapter at the end. It
 */
class ListStatusRecyclerViewAdapter<T>(var lifecycleOwner: LifecycleOwner?): ListAdapter<RepositoryLoadHelper.RepoLoadStatus, ListStatusRecyclerViewAdapter.StatusViewHolder>(LOAD_STATUS_DIFF_UTIL),
        Observer<RepositoryLoadHelper.RepoLoadStatus> {

    private inner class ListStatusMediatorLiveData: MediatorLiveData<RepositoryLoadHelper.RepoLoadStatus>() {

        private var currentPagedList: PagedList<*>? = null

        private var currentLoadStatus: RepositoryLoadHelper.RepoLoadStatus? = null

        var pagedListLiveData: LiveData<PagedList<T>>? = null
            set(value) {
                field?.also {
                    removeSource(it)
                }

                field = value
                value?.also {
                    addSource(it) {
                        currentPagedList = it
                        emitLoadStatus()
                    }
                }
            }

        var repoLoadStatus: LiveData<RepositoryLoadHelper.RepoLoadStatus>? = null
            set(value) {
                field?.also {
                    removeSource(it)
                }

                field = value

                value?.also {
                    addSource(it) {
                        currentLoadStatus = it
                        emitLoadStatus()
                    }
                }
            }


        private fun emitLoadStatus() {
            value = if(value?.loadStatus == STATUS_LOADED_NODATA && currentPagedList?.isNotEmpty() ?: false) {
                RepositoryLoadHelper.RepoLoadStatus(STATUS_LOADED_WITHDATA)
            }else {
                currentLoadStatus
            }
        }

    }

    class StatusViewHolder(val binding: ItemListStatusBinding) : RecyclerView.ViewHolder(binding.root)

    private var mediatorLiveData: ListStatusMediatorLiveData? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mediatorLiveData = ListStatusMediatorLiveData().also {
            lifecycleOwner?.also { lifecycle -> it.observe(lifecycle, this) }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mediatorLiveData?.also {
            it.removeObserver(this)
        }
        mediatorLiveData = null
    }

    override fun onChanged(t: RepositoryLoadHelper.RepoLoadStatus?) {
        if(t != null && t.loadStatus != STATUS_LOADED_WITHDATA) {
            submitList(listOf(t))
        }else {
            submitList(emptyList())
        }
    }

    var repositoryLoadStatus: LiveData<RepositoryLoadHelper.RepoLoadStatus>?
        get() = mediatorLiveData?.repoLoadStatus
        set(value) {
            mediatorLiveData?.repoLoadStatus = value
        }

    var pagedListLiveData: LiveData<PagedList<T>>?
        get() = mediatorLiveData?.pagedListLiveData
        set(value) {
            mediatorLiveData?.pagedListLiveData = value
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val viewHolder = StatusViewHolder(ItemListStatusBinding.inflate(LayoutInflater.from(parent.context),
            parent, false).also {
            it.emptyStateMessage = parent.context.getString(R.string.nothing_here)
        })

        return viewHolder
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        holder.binding.loadingStatus = getItem(position)
    }

    companion object {
        val LOAD_STATUS_DIFF_UTIL = object: DiffUtil.ItemCallback<RepositoryLoadHelper.RepoLoadStatus>() {
            override fun areItemsTheSame(oldItem: RepositoryLoadHelper.RepoLoadStatus, newItem: RepositoryLoadHelper.RepoLoadStatus): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: RepositoryLoadHelper.RepoLoadStatus, newItem: RepositoryLoadHelper.RepoLoadStatus): Boolean {
                return oldItem.loadStatus == newItem.loadStatus
            }
        }

        @JvmField
        val MAP_STATUS_STRINGS = mapOf(
                RepositoryLoadHelper.STATUS_LOADING_CLOUD to MessageID.repo_loading_status_loading_cloud,
                RepositoryLoadHelper.STATUS_LOADING_MIRROR to MessageID.repo_loading_status_loading_mirror,
                RepositoryLoadHelper.STATUS_FAILED_CONNECTION_ERR to MessageID.repo_loading_status_failed_connection_error)

    }

}