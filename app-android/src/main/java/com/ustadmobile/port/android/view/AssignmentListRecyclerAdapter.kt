
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemAssignmentListBinding
import com.ustadmobile.core.controller.ClazzAssignmentListItemListener

import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.port.android.view.ext.setSelectedIfInList


class AssignmentListRecyclerAdapter(var itemListener: ClazzAssignmentListItemListener?): SelectablePagedListAdapter<ClazzAssignmentWithMetrics, AssignmentListRecyclerAdapter.AssignmentListViewHolder>(DIFF_CALLBACK) {

    class AssignmentListViewHolder(val itemBinding: ItemAssignmentListBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentListViewHolder {
        val itemBinding = ItemAssignmentListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.itemListener = itemListener
        itemBinding.selectablePagedListAdapter = this
        return AssignmentListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: AssignmentListViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.assignment = item
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzAssignmentWithMetrics> = object
            : DiffUtil.ItemCallback<ClazzAssignmentWithMetrics>() {
            override fun areItemsTheSame(oldItem: ClazzAssignmentWithMetrics,
                                         newItem: ClazzAssignmentWithMetrics): Boolean {
                return oldItem.caUid == newItem.caUid
            }

            override fun areContentsTheSame(oldItem: ClazzAssignmentWithMetrics,
                                            newItem: ClazzAssignmentWithMetrics): Boolean {
                return oldItem == newItem
            }
        }
    }

}