package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemClazzAssignmentWithMetricsBinding
import com.ustadmobile.core.controller.ClazzAssignmentListPresenter
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics

class ClazzAssignmentListRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<ClazzAssignmentWithMetrics>,
        internal var mPresenter: ClazzAssignmentListPresenter?)
    : PagedListAdapter<ClazzAssignmentWithMetrics,
        ClazzAssignmentListRecyclerAdapter.ClazzAssignmentListViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzAssignmentListViewHolder {
        val clazzAssignmentListBinding = ItemClazzAssignmentWithMetricsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        clazzAssignmentListBinding.presenter = mPresenter

        return ClazzAssignmentListViewHolder(clazzAssignmentListBinding)

    }

    override fun onBindViewHolder(holder: ClazzAssignmentListViewHolder, position: Int) {

        val entity = getItem(position)
        holder.binding.clazzassignmentwithmetrics = entity

        if(entity != null) {
            holder.binding.itemClazzAssignmentWithMetricsDate.text =
                    UMCalendarUtil.getPrettyDateSimpleFromLong(entity.clazzAssignmentDueDate, null).toString()
            val descText = entity.notStartedStudents.toString() + " " +
                    holder.itemView.context.getString(R.string.not_started) + ", " +
                    entity.startedStudents + " " + holder.itemView.context.getString(R.string.started) + ", " +
                    entity.completedStudents + " " + holder.itemView.context.getString(R.string.completed)
            holder.binding.itemClazzAssignmentWithMetricsDescription.text = descText

        }
    }

    inner class ClazzAssignmentListViewHolder
    internal constructor(val binding: ItemClazzAssignmentWithMetricsBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mPresenter = null
    }
}
