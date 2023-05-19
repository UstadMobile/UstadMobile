package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.View
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
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_FAILED_CONNECTION_ERR
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_FAILED_NOCONNECTIVITYORPEERS
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADED_NODATA
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADED_WITHDATA
import com.ustadmobile.door.RepositoryLoadHelper.Companion.STATUS_LOADING_CLOUD


/**
 * This RecyclerViewAdapter is intended to be placed in a MergeAdapter at the end. It
 */
class ListStatusRecyclerViewAdapter<T: Any>(var lifecycleOwner: LifecycleOwner?,
        val emptyStateString: String? = null,
        val emptyStateDrawableId: Int = R.drawable.ic_empty): ListAdapter<RepositoryLoadHelper.RepoLoadStatus, ListStatusRecyclerViewAdapter.StatusViewHolder>(LOAD_STATUS_DIFF_UTIL),
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
            val currentLoadStatusFlag = currentLoadStatus?.loadStatus ?: -1
            value = if(currentLoadStatusFlag in STATUSES_TO_HIDE_IF_LOCALDATA_LOADED && !currentPagedList.isNullOrEmpty()) {
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
            it.emptyStateMessage = emptyStateString ?: parent.context.getString(R.string.nothing_here)
            it.emptyStateDrawableId = emptyStateDrawableId
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

        val STATUSES_TO_HIDE_IF_LOCALDATA_LOADED = listOf(STATUS_FAILED_CONNECTION_ERR,
            STATUS_FAILED_NOCONNECTIVITYORPEERS, STATUS_LOADED_NODATA)

        @JvmField
        val MAP_STATUS_STRINGS = mapOf(
                STATUS_LOADING_CLOUD to MessageID.repo_loading_status_loading_cloud,
                STATUS_FAILED_CONNECTION_ERR to MessageID.repo_loading_status_failed_connection_error,
                STATUS_FAILED_NOCONNECTIVITYORPEERS to MessageID.repo_loading_status_failed_noconnection)

        @JvmField
        val MAP_ICON_IMAGEIDS = mapOf(
                STATUS_LOADING_CLOUD to R.drawable.ic_cloud_download_black_24dp,
                STATUS_FAILED_CONNECTION_ERR to R.drawable.ic_error_black_24dp,
                STATUS_FAILED_NOCONNECTIVITYORPEERS to R.drawable.ic_signal_cellular_connected_no_internet_4_bar_black_24dp)

    }

}