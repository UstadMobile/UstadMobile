package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemContentWithMetricsBinding
import com.ustadmobile.core.controller.ClazzAssignmentDetailAssignmentPresenter
import com.ustadmobile.lib.db.entities.ContentEntryWithMetrics

class ContentEntryWithMetricsRecyclerAdapter(
        diffCallback: DiffUtil.ItemCallback<ContentEntryWithMetrics>)
    : PagedListAdapter<ContentEntryWithMetrics,
        ContentEntryWithMetricsRecyclerAdapter.ClazzAssignmentListViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzAssignmentListViewHolder {
        val clazzAssignmentListBinding = ItemContentWithMetricsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)

        return ClazzAssignmentListViewHolder(clazzAssignmentListBinding)

    }

    override fun onBindViewHolder(holder: ClazzAssignmentListViewHolder, position: Int) {

        val entity = getItem(position)
        holder.binding.contententrywithmetrics = entity
    }

    inner class ClazzAssignmentListViewHolder
    internal constructor(val binding: ItemContentWithMetricsBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
    }
}
