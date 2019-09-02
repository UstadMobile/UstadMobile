package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.networkmanager.LocalAvailabilityListener
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor
import com.ustadmobile.core.networkmanager.OnDownloadJobItemChangeListener
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.lib.db.entities.ContentEntryWithStatusAndMostRecentContainerUid
import com.ustadmobile.lib.db.entities.DownloadJobItemStatus
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class ContentEntryListRecyclerViewAdapter internal constructor(private val activity: FragmentActivity,
                                                               private val listener: AdapterViewListener,
                                                               private val monitor: LocalAvailabilityMonitor?,
                                                               private val managerAndroidBle: NetworkManagerBle)
    : PagedListAdapter<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer, ContentEntryListRecyclerViewAdapter.ViewHolder>(DIFF_CALLBACK),
        LocalAvailabilityListener, OnDownloadJobItemChangeListener {

    private val containerUidsToMonitor = HashSet<Long>()

    private val boundViewHolders: MutableSet<ViewHolder>

    private var emptyStateListener: EmptyStateListener? = null

    /**
     * @return List of container uids that can be monitored (Requires status).
     */
    private val uniqueContainerUidsListTobeMonitored: List<Long>
        get() {
            val currentDisplayedEntryList = if (currentList == null) ArrayList<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>() else currentList
            val uidsToMonitor = ArrayList<Long>()
            for (entry in currentDisplayedEntryList!!) {

                val canBeMonitored = (entry != null && (entry.contentEntryStatus == null || entry.contentEntryStatus!!.downloadStatus != JobStatus.COMPLETE)
                        && !containerUidsToMonitor.contains(entry.mostRecentContainer)
                        && entry.leaf)
                if (canBeMonitored) {
                    uidsToMonitor.add(entry!!.mostRecentContainer)
                }

            }
            return uidsToMonitor
        }

    init {
        boundViewHolders = HashSet()
    }


    fun addListeners() {
        managerAndroidBle.addLocalAvailabilityListener(this)
        managerAndroidBle.addDownloadChangeListener(this)
    }

    fun removeListeners() {
        managerAndroidBle.removeLocalAvailabilityListener(this)
        managerAndroidBle.removeDownloadChangeListener(this)
    }

    fun setEmptyStateListener(stateListener: EmptyStateListener) {
        this.emptyStateListener = stateListener
    }

    override fun onLocalAvailabilityChanged(locallyAvailableEntries: Set<Long>) {

        val viewHoldersToNotify: List<ViewHolder>
        synchronized(boundViewHolders) {
            viewHoldersToNotify = LinkedList(boundViewHolders)
        }

        for (viewHolder in viewHoldersToNotify) {
            val available = locallyAvailableEntries.contains(viewHolder.containerUid)
            UMLog.l(UMLog.DEBUG, 694,
                    "Entry status check received  $available")
            activity.runOnUiThread { viewHolder.updateLocallyAvailabilityStatus(available) }
        }
    }

    override fun onDownloadJobItemChange(status: DownloadJobItemStatus?, downloadJobUid: Int) {
        val holdersToNotify: List<ViewHolder>
        synchronized(boundViewHolders) {
            holdersToNotify = LinkedList(boundViewHolders)
        }

        for (viewHolder in holdersToNotify) {
            viewHolder.onDownloadJobItemChange(status)
        }
    }

    interface AdapterViewListener {
        fun contentEntryClicked(entry: ContentEntry?)

        fun downloadStatusClicked(entry: ContentEntry?)
    }

    interface EmptyStateListener {

        fun onEntriesLoaded()
    }

    override fun onViewRecycled(holder: ViewHolder) {
        synchronized(boundViewHolders) {
            boundViewHolders.remove(holder)
        }

        super.onViewRecycled(holder)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_content_entry, parent, false)
        return ViewHolder(view)
    }


    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        if (monitor != null) {
            containerUidsToMonitor.clear()
        }
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = getItem(position)

        emptyStateListener!!.onEntriesLoaded()

        synchronized(boundViewHolders) {
            boundViewHolders.add(holder)
        }

        if (entry == null) {
            holder.containerUid = 0L
            holder.contentEntryUid = 0L
            holder.entryTitle.text = ""
            holder.entryDescription.text = ""
            holder.thumbnailView.setImageDrawable(null)
            holder.downloadView.progress = 0
            holder.downloadView.setImageResource(R.drawable.ic_file_download_black_24dp)
            holder.view.setOnClickListener(null)
            holder.downloadView.setOnClickListener(null)
            holder.availabilityStatus.text = ""
            holder.availabilityIcon.setImageDrawable(null)
        } else {
            val available: Boolean = managerAndroidBle.isEntryLocallyAvailable(
                    entry.mostRecentContainer)

            if (entry.leaf) {
                holder.updateLocallyAvailabilityStatus(available)
            }

            holder.containerUid = entry.mostRecentContainer
            holder.contentEntryUid = entry.contentEntryUid

            holder.view.tag = entry.contentEntryUid
            holder.entryTitle.text = entry.title
            holder.entryDescription.text = entry.description
            if (entry.thumbnailUrl == null || entry.thumbnailUrl!!.isEmpty()) {
                holder.thumbnailView.setImageDrawable(null)
            } else {
                UMAndroidUtil.loadImage(entry.thumbnailUrl,R.drawable.img_placeholder,
                        holder.thumbnailView)
            }


            var contentDescription: String? = null
            var showLocallyAvailabilityViews = true
            if (entry.contentEntryStatus != null) {
                val context = holder.view.context
                val status = entry.contentEntryStatus
                val dlStatus = status!!.downloadStatus

                contentDescription = if (dlStatus > 0 && dlStatus <= JobStatus.RUNNING_MAX && status.totalSize > 0) {
                    context.getString(R.string.downloading)
                } else {
                    context.getString(R.string.download_entry_state_queued)
                }

                if (dlStatus > 0 && dlStatus < JobStatus.WAITING_MAX) {
                    holder.downloadView.setImageResource(R.drawable.ic_pause_black_24dp)
                    contentDescription = context.getString(R.string.download_entry_state_paused)
                } else if (dlStatus == JobStatus.COMPLETE) {
                    showLocallyAvailabilityViews = false
                    holder.downloadView.setImageResource(R.drawable.ic_offline_pin_black_24dp)
                    contentDescription = context.getString(R.string.downloaded)
                } else {
                    holder.downloadView.setImageResource(R.drawable.ic_file_download_black_24dp)
                }
            } else {
                holder.downloadView.progress = 0
                holder.downloadView.setImageResource(R.drawable.ic_file_download_black_24dp)
            }


            val iconView = holder.iconView
            val iconFlag = entry.contentTypeFlag
            iconView.setImageResource(
                    if (CONTENT_TYPE_TO_ICON_RES_MAP.containsKey(entry.contentTypeFlag))
                        CONTENT_TYPE_TO_ICON_RES_MAP[entry.contentTypeFlag]!!
                    else
                        R.drawable.ic_book_black_24dp)

            if (iconFlag == ContentEntry.UNDEFINED_TYPE) {
                iconView.visibility = View.GONE
            } else {
                iconView.visibility = View.VISIBLE
            }

            val viewVisibility = if (showLocallyAvailabilityViews && entry.leaf)
                View.VISIBLE
            else
                View.GONE
            holder.availabilityIcon.visibility = viewVisibility
            holder.availabilityStatus.visibility = viewVisibility

            val containerUidList = uniqueContainerUidsListTobeMonitored
            if (containerUidList.isNotEmpty()) {
                containerUidsToMonitor.addAll(containerUidList)
                monitor!!.startMonitoringAvailability(monitor, containerUidList)
            }

            holder.downloadView.imageResource!!.contentDescription = contentDescription
            holder.view.setOnClickListener { listener.contentEntryClicked(entry) }
            holder.downloadView.setOnClickListener { listener.downloadStatusClicked(entry) }
            holder.downloadView.progress = 0
            GlobalScope.launch(Dispatchers.Main) {
                val downloadJobItemStatus = managerAndroidBle.findDownloadJobItemStatusByContentEntryUid(
                    entry.contentEntryUid)
                if(downloadJobItemStatus != null){
                    holder.downloadView.progressVisibility = View.VISIBLE
                    holder.onDownloadJobItemChange(downloadJobItemStatus)
                }else {
                    holder.downloadView.progressVisibility = View.INVISIBLE
                }
            }

        }
    }

    inner class ViewHolder internal constructor(val view: View) : RecyclerView.ViewHolder(view) {
        internal val entryTitle: TextView = view.findViewById(R.id.content_entry_item_title)
        internal val entryDescription: TextView = view.findViewById(R.id.content_entry_item_description)
        private val entrySize: TextView = view.findViewById(R.id.content_entry_item_library_size)
        internal val thumbnailView: ImageView = view.findViewById(R.id.content_entry_item_thumbnail)
        val availabilityIcon: ImageView = view.findViewById(R.id.content_entry_local_availability_icon)
        val availabilityStatus: TextView = view.findViewById(R.id.content_entry_local_availability_status)
        val downloadView: DownloadStatusButton = view.findViewById(R.id.content_entry_item_download)
        val iconView: ImageView = view.findViewById(R.id.content_entry_item_imageview)

        internal var containerUid: Long = 0

        var contentEntryUid: Long = 0

        internal fun updateLocallyAvailabilityStatus(available: Boolean) {
            val icon = if (available)
                R.drawable.ic_nearby_black_24px
            else
                R.drawable.ic_cloud_download_black_24dp
            val status = if (available)
                R.string.download_locally_availability
            else
                R.string.download_cloud_availability
            availabilityIcon.setImageResource(icon)
            availabilityStatus.text = view.context.getString(status)
        }

        override fun toString(): String {
            return super.toString() + " '" + entryDescription.text + "'"
        }

        internal fun onDownloadJobItemChange(status: DownloadJobItemStatus?) {
            if (status != null && status.contentEntryUid == contentEntryUid) {
                UMLog.l(UMLog.DEBUG, 420, "ContentEntryList update " +
                        "entryUid " + status.contentEntryUid)
                activity.runOnUiThread {
                    downloadView.progress = if (status.totalBytes > 0)
                        (status.bytesSoFar * 100 / status.totalBytes).toInt()
                    else
                        0

                    if (status.totalBytes > 0) {
                        if (status.bytesSoFar == status.totalBytes) {
                            /*
                             * ContentEntryStatus will be changed, and that will trigger showing
                             * the offline downloaded pin. We can now hide the progress view.
                             */
                            downloadView.progressVisibility = View.INVISIBLE
                        } else if (status.totalBytes > 0 && downloadView.progressVisibility != View.VISIBLE) {
                            /*
                             * The download just started. When this view was first shown, the download
                             * was not in progress, so the progress view was made invisible. We need
                             * to show it now that the download is underway.
                             */
                            downloadView.progressVisibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    companion object {

        private val CONTENT_TYPE_TO_ICON_RES_MAP = HashMap<Int, Int>()

        init {
            CONTENT_TYPE_TO_ICON_RES_MAP[ContentEntry.EBOOK_TYPE] = R.drawable.ic_book_black_24dp
            CONTENT_TYPE_TO_ICON_RES_MAP[ContentEntry.AUDIO_TYPE] = R.drawable.ic_audiotrack_24px
            CONTENT_TYPE_TO_ICON_RES_MAP[ContentEntry.VIDEO_TYPE] = R.drawable.ic_video_library_24px
            CONTENT_TYPE_TO_ICON_RES_MAP[ContentEntry.COLLECTION_TYPE] = R.drawable.ic_collections_24px
            CONTENT_TYPE_TO_ICON_RES_MAP[ContentEntry.DOCUMENT_TYPE] = R.drawable.ic_file_24px
            CONTENT_TYPE_TO_ICON_RES_MAP[ContentEntry.INTERACTIVE_EXERICSE_TYPE] = R.drawable.ic_assignment_24px
            CONTENT_TYPE_TO_ICON_RES_MAP[ContentEntry.ARTICLE_TYPE] = R.drawable.ic_newspaper
        }

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>() {
            override fun areItemsTheSame(oldItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
                                         newItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer): Boolean {
                return oldItem.contentEntryUid == newItem.contentEntryUid
            }

            override fun areContentsTheSame(oldItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
                                            newItem: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer): Boolean {
                if (if (oldItem.title != null) oldItem.title != newItem.title else newItem.title != null) {
                    return false
                }
                if (if (oldItem.description != null) oldItem.description != newItem.description else newItem.description != null)
                    return false
                if (if (oldItem.thumbnailUrl != null) oldItem.thumbnailUrl != newItem.thumbnailUrl else newItem.thumbnailUrl == null) {
                    return false
                }
                if (oldItem.contentEntryStatus != null && newItem.contentEntryStatus != null) {

                    if (oldItem.contentEntryStatus!!.bytesDownloadSoFar != newItem.contentEntryStatus!!.bytesDownloadSoFar) {
                        return false
                    }

                    return if (oldItem.contentEntryStatus!!.downloadStatus != newItem.contentEntryStatus!!.downloadStatus) {
                        false
                    } else oldItem.contentEntryStatus!!.totalSize == newItem.contentEntryStatus!!.totalSize

                } else
                    return newItem.contentEntryStatus == null && newItem.contentEntryStatus == null || newItem.contentEntryStatus == oldItem.contentEntryStatus
            }
        }
    }
}
