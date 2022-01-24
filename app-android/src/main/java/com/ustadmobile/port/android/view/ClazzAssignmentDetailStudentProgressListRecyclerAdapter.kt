
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.core.controller.ClazzAssignmentDetailStudentProgressListItemListener
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics

import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.toughra.ustadmobile.databinding.ItemClazzAssignmentDetailStudentProgressOverviewListBinding
import com.ustadmobile.port.android.view.ext.setSelectedIfInList


class ClazzAssignmentDetailStudentProgressListRecyclerAdapter(var itemListener: ClazzAssignmentDetailStudentProgressListItemListener?): SelectablePagedListAdapter<ClazzAssignmentWithMetrics, ClazzAssignmentDetailStudentProgressListRecyclerAdapter.ClazzAssignmentWithMetricsListViewHolder>(DIFF_CALLBACK) {

    class ClazzAssignmentWithMetricsListViewHolder(val itemBinding: ItemClazzAssignmentDetailStudentProgressOverviewListBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzAssignmentWithMetricsListViewHolder {
        val itemBinding = ItemClazzAssignmentDetailStudentProgressOverviewListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.itemListener = itemListener
        itemBinding.selectablePagedListAdapter = this
        return ClazzAssignmentWithMetricsListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ClazzAssignmentWithMetricsListViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.clazzAssignmentWithMetrics = item
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