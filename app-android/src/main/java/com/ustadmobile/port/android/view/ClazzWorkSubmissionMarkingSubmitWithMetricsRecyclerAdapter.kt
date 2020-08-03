package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzworksubmissionMarkingButtonWithExtraBinding
import com.ustadmobile.core.controller.ClazzWorkSubmissionMarkingPresenter
import com.ustadmobile.lib.db.entities.ClazzMemberAndClazzWorkWithSubmission
import com.ustadmobile.lib.db.entities.ClazzWorkWithMetrics

class ClazzWorkSubmissionMarkingSubmitWithMetricsRecyclerAdapter(clazzWork: ClazzWorkWithMetrics?,
                     clazzMemberAndClazzWorkWithSubmission: ClazzMemberAndClazzWorkWithSubmission?,
                     presenter: ClazzWorkSubmissionMarkingPresenter?,
                     visible: Boolean = true, markingLeft: Boolean)
    : ListAdapter<ClazzWorkWithMetrics,
        ClazzWorkSubmissionMarkingSubmitWithMetricsRecyclerAdapter.ClazzWorkProgressViewHolder>(
        ClazzWorkDetailProgressListFragment.DU_CLAZZWORKWITHMETRICS) {

    var visible: Boolean = visible
        set(value) {
            if(field == value)
                return

            field = value
        }


    var showNext: Boolean = markingLeft
        set(value) {
            if(field == value)
                return

            field = value
        }

    class ClazzWorkProgressViewHolder(var itemBinding: ItemClazzworksubmissionMarkingButtonWithExtraBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: ClazzWorkProgressViewHolder? = null
    private var clazzWorkVal : ClazzWorkWithMetrics? = clazzWork
    private var mPresenter : ClazzWorkSubmissionMarkingPresenter? = presenter
    var passThis: ClazzMemberAndClazzWorkWithSubmission? = clazzMemberAndClazzWorkWithSubmission

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzWorkProgressViewHolder {
        return ClazzWorkProgressViewHolder(
                ItemClazzworksubmissionMarkingButtonWithExtraBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.mPresenter = mPresenter
                    it.clazzWorkWithMetrics = clazzWorkVal
                    it.showNext = showNext
                    it.clazzMemberAndClazzWorkWithSubmission = passThis
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun getItemCount(): Int {
        return if(visible) 1 else 0
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