package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemContentWithAttemptBinding
import com.ustadmobile.core.controller.ContentWithAttemptListener
import com.ustadmobile.lib.db.entities.ContentWithAttemptSummary
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class ContentWithAttemptRecyclerAdapter(var listener: ContentWithAttemptListener?)
    : SelectablePagedListAdapter<ContentWithAttemptSummary, ContentWithAttemptRecyclerAdapter.ContentWithAttemptViewHolder>(DIFF_CALLBACK) {

    inner class ContentWithAttemptViewHolder(val itemBinding: ItemContentWithAttemptBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentWithAttemptViewHolder {
        val itemBinding = ItemContentWithAttemptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.selectablePagedListAdapter = this
        return ContentWithAttemptViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ContentWithAttemptViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.contentEntry = item
        holder.itemBinding.listener = listener
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    companion object {

        val DIFF_CALLBACK: DiffUtil.ItemCallback<ContentWithAttemptSummary> = object
            : DiffUtil.ItemCallback<ContentWithAttemptSummary>() {
            override fun areItemsTheSame(oldItem: ContentWithAttemptSummary,
                                         newItem: ContentWithAttemptSummary): Boolean {
                return oldItem.contentEntryUid == newItem.contentEntryUid
            }

            override fun areContentsTheSame(oldItem: ContentWithAttemptSummary,
                                            newItem: ContentWithAttemptSummary): Boolean {
                return oldItem.contentEntryTitle == newItem.contentEntryTitle &&
                        oldItem.contentEntryUid == newItem.contentEntryUid
            }

        }

    }



}
