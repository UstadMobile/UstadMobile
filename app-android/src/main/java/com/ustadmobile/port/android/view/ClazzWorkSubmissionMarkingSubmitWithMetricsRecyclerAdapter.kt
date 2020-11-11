package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzworksubmissionMarkingButtonWithExtraBinding
import com.ustadmobile.core.controller.ClazzWorkSubmissionMarkingPresenter
import com.ustadmobile.lib.db.entities.ClazzWorkWithMetrics

class ClazzWorkSubmissionMarkingSubmitWithMetricsRecyclerAdapter(clazzWork: ClazzWorkWithMetrics?,
                     presenter: ClazzWorkSubmissionMarkingPresenter?, markingLeft: Boolean)
    : ListAdapter<ClazzWorkWithMetrics,
        ClazzWorkSubmissionMarkingSubmitWithMetricsRecyclerAdapter.ClazzWorkProgressViewHolder>(
        ClazzWorkDetailProgressListFragment.DU_CLAZZWORKWITHMETRICS) {


    var showNext: Boolean = markingLeft

    class ClazzWorkProgressViewHolder(var itemBinding: ItemClazzworksubmissionMarkingButtonWithExtraBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: ClazzWorkProgressViewHolder? = null
    private var clazzWorkVal : ClazzWorkWithMetrics? = clazzWork
    private var mPresenter : ClazzWorkSubmissionMarkingPresenter? = presenter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzWorkProgressViewHolder {
        return ClazzWorkProgressViewHolder(
                ItemClazzworksubmissionMarkingButtonWithExtraBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.mPresenter = mPresenter
                    it.clazzWorkWithMetrics = clazzWorkVal
                    it.showNext = showNext
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: ClazzWorkProgressViewHolder, position: Int) {

        holder.itemView.tag = clazzWorkVal?.clazzWorkUid?:0L
        if(currentList.size > 0 && getItem(position) != null){
            holder.itemBinding.clazzWorkWithMetrics = getItem(0)
            holder.itemView.tag = getItem(position).clazzWorkUid
        }else {
            holder.itemBinding.clazzWorkWithMetrics = clazzWorkVal
        }
        holder.itemBinding.showNext = showNext
    }
}