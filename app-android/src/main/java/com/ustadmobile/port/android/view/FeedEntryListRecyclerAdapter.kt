
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemFeedEntryListBinding
import com.ustadmobile.core.controller.FeedEntryListItemListener
import com.ustadmobile.lib.db.entities.FeedEntry

import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ustadmobile.port.android.view.ext.setSelectedIfInList


class FeedEntryListRecyclerAdapter(var itemListener: FeedEntryListItemListener?): SelectablePagedListAdapter<FeedEntry, FeedEntryListRecyclerAdapter.FeedEntryListViewHolder>(DIFF_CALLBACK) {

    class FeedEntryListViewHolder(val itemBinding: ItemFeedEntryListBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedEntryListViewHolder {
        val itemBinding = ItemFeedEntryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.itemListener = itemListener
        itemBinding.selectablePagedListAdapter = this
        return FeedEntryListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: FeedEntryListViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.feedEntry = item
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<FeedEntry> = object
            : DiffUtil.ItemCallback<FeedEntry>() {
            override fun areItemsTheSame(oldItem: FeedEntry,
                                         newItem: FeedEntry): Boolean {
                return oldItem.feUid == newItem.feUid
            }

            override fun areContentsTheSame(oldItem: FeedEntry,
                                            newItem: FeedEntry): Boolean {
                return oldItem.feTitle == newItem.feTitle
                        && oldItem.feDescription == newItem.feDescription
                        && oldItem.feViewDest == newItem.feViewDest
            }
        }
    }

}