package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemContentEntryListBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ContentEntryListItemListener
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.RateLimitedLiveData
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.lib.db.entities.ContentJobItemProgressAndStatus
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class ContentEntryListRecyclerAdapter(
    var itemListener: ContentEntryListItemListener?,
    private val pickerMode: String?,
    private val selectFolderVisible: Boolean?,
    /**
    * The lifecycle owner is needed for use of livedata observers
    * Unfortunately findViewTreeLifecycleOwner is flaky when used
    * with a recyclerview item
    */
    private var lifecycleOwner: LifecycleOwner?,
    di: DI
) : SelectablePagedListAdapter<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
    ContentEntryListRecyclerAdapter.ContentEntryListViewHolder>(ContentEntryList2Fragment.DIFF_CALLBACK)
{

    private val accountManager: UstadAccountManager by di.instance()

    private val appDatabase: UmAppDatabase by di.on(accountManager.activeAccount).instance(tag = UmAppDatabase.TAG_DB)

    private val boundViewHolders = mutableSetOf<ContentEntryListViewHolder>()

    inner class ContentEntryListViewHolder(
        val itemBinding: ItemContentEntryListBinding
    ): RecyclerView.ViewHolder(itemBinding.root), DoorObserver<ContentJobItemProgressAndStatus?>{
        var downloadJobItemLiveData: DoorLiveData<ContentJobItemProgressAndStatus?>? = null
            set(value) {
                field?.removeObserver(this)
                field = value
                lifecycleOwner?.also {
                    value?.observe(it, this)
                }
            }


        override fun onChanged(t: ContentJobItemProgressAndStatus?) {
            if(t != null) {
                itemBinding.downloadStatusButton.contentJobItemStatus = t.status

                if(t.total > 0){
                    itemBinding.downloadStatusButton.progress = ((t.progress * 100) / t.total).toInt()
                }
            }else {
                itemBinding.downloadStatusButton.progress = 0
                itemBinding.downloadStatusButton.contentJobItemStatus = 0
            }
        }
    }

    fun onLocalAvailabilityUpdated(localAvailabilityMap: Map<Long, Boolean>) {
        boundViewHolders.forEach {
            it.itemBinding.locallyAvailable = localAvailabilityMap.getOrElse(
                    it.itemBinding.contentEntry?.mostRecentContainer?.containerUid ?: -1) { false }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentEntryListViewHolder {
        val itemBinding = ItemContentEntryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.itemListener = itemListener
        itemBinding.selectablePagedListAdapter = this
        itemBinding.isPickerMode = pickerMode == ListViewMode.PICKER.toString()
        itemBinding.selectFolderVisible = selectFolderVisible ?: true
        return ContentEntryListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ContentEntryListViewHolder, position: Int) {
        val item = getItem(position)
        boundViewHolders += holder
        holder.itemBinding.contentEntry = item
        holder.itemView.setSelectedIfInList(item, selectedItems, ContentEntryList2Fragment.DIFF_CALLBACK)
        if(item != null) {
            holder.downloadJobItemLiveData = RateLimitedLiveData(appDatabase, listOf("ContentJobItem"), 1000) {
                appDatabase.contentEntryDao.statusForContentEntryList(item.contentEntryUid)
            }
        }else{
            holder.downloadJobItemLiveData = null
        }
    }

    override fun onViewRecycled(holder: ContentEntryListViewHolder) {
        boundViewHolders -= holder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
        lifecycleOwner = null
    }
}