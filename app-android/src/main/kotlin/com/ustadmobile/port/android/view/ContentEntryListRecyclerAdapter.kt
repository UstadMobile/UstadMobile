package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemContentEntryBinding
import com.ustadmobile.lib.db.entities.ClazzAssignmentContentEntryJoinWithContentEntry

class ContentEntryListRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<ClazzAssignmentContentEntryJoinWithContentEntry>)
    : ListAdapter<ClazzAssignmentContentEntryJoinWithContentEntry,
        ContentEntryListRecyclerAdapter.ClazzAssignmentListViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzAssignmentListViewHolder {
        val clazzAssignmentListBinding = ItemContentEntryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)

        return ClazzAssignmentListViewHolder(clazzAssignmentListBinding)

    }

    override fun onBindViewHolder(holder: ClazzAssignmentListViewHolder, position: Int) {

        val entity = getItem(position)
        holder.binding.contententry = entity.contentEntry
    }

    inner class ClazzAssignmentListViewHolder
    internal constructor(val binding: ItemContentEntryBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
    }
}
