package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemContentEntryListBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ContentEntryListItemListener
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

class ContentEntryListRecyclerAdapter(var itemListener: ContentEntryListItemListener?,
                                      private val pickerMode: String?,
                                      /**
                                       * The lifecycle owner is needed for use of livedata observers
                                       * Unfortunately findViewTreeLifecycleOwner is flaky when used
                                       * with a recyclerview item
                                       */
                                      private var lifecycleOwner: LifecycleOwner?, di: DI)
    : SelectablePagedListAdapter<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer, ContentEntryListRecyclerAdapter.ContentEntryListViewHolder>(ContentEntryList2Fragment.DIFF_CALLBACK) {

    val accountManager: UstadAccountManager by di.instance()

    val containerDownloadManager: ContainerDownloadManager by di.on(accountManager.activeAccount).instance()

    inner class ContentEntryListViewHolder(val itemBinding: ItemContentEntryListBinding): RecyclerView.ViewHolder(itemBinding.root),
        DoorObserver<DownloadJobItem?>{
        var downloadJobItemLiveData: DoorLiveData<DownloadJobItem?>? = null
            set(value) {
                field?.removeObserver(this)
                field = value
                lifecycleOwner?.also {
                    value?.observe(it, this)
                }
            }


        override fun onChanged(t: DownloadJobItem?) {
            itemBinding.downloadStatusButton.downloadJobItem = t
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentEntryListViewHolder {
        val itemBinding = ItemContentEntryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.itemListener = itemListener
        itemBinding.selectablePagedListAdapter = this
        itemBinding.isPickerMode = pickerMode == ListViewMode.PICKER.toString()
        return ContentEntryListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ContentEntryListViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.contentEntry = item
        holder.itemView.setSelectedIfInList(item, selectedItems, ContentEntryList2Fragment.DIFF_CALLBACK)
        //holder.downloadJobItemLiveData = null
        if(item != null) {
            GlobalScope.launch(Dispatchers.Main.immediate) {
                holder.downloadJobItemLiveData = containerDownloadManager
                        .getDownloadJobItemByContentEntryUid(item.contentEntryUid )
            }
        }

    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
        lifecycleOwner = null
    }
}