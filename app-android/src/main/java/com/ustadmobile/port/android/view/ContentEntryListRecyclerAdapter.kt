package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemContentEntryListBinding
import com.ustadmobile.core.controller.ContentEntryListItemListener
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class ContentEntryListRecyclerAdapter(var itemListener: ContentEntryListItemListener?, private val pickerMode: String?)
    : SelectablePagedListAdapter<ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer, ContentEntryListRecyclerAdapter.ContentEntryListViewHolder>(ContentEntryList2Fragment.DIFF_CALLBACK) {

    class ContentEntryListViewHolder(val itemBinding: ItemContentEntryListBinding): RecyclerView.ViewHolder(itemBinding.root)


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
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }
}