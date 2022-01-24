package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemAssignmentProgressSummaryDetailBinding
import com.ustadmobile.lib.db.entities.AssignmentProgressSummary
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter


class AssignmentProgressSummaryRecyclerAdapter(progressSummary: AssignmentProgressSummary?)
    : SingleItemRecyclerViewAdapter<AssignmentProgressSummaryRecyclerAdapter.AssignmentProgressSummaryViewHolder>() {

    class AssignmentProgressSummaryViewHolder(var itemBinding: ItemAssignmentProgressSummaryDetailBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: AssignmentProgressSummaryViewHolder? = null

    var assignmentProgressSummaryVal: AssignmentProgressSummary? = progressSummary
        set(value){
            field = value
            visible = value?.hasMetricsPermission ?: false
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentProgressSummaryViewHolder {
        return AssignmentProgressSummaryViewHolder(
                ItemAssignmentProgressSummaryDetailBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: AssignmentProgressSummaryViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemBinding.assignmentProgressSummary = assignmentProgressSummaryVal
    }

}