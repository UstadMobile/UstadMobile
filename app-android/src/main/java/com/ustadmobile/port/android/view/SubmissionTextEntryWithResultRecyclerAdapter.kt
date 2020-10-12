package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzworkSubmissionTextEntryBinding
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.ClazzWorkWithSubmission

class SubmissionTextEntryWithResultRecyclerAdapter(visible: Boolean = false,
                                                   marking: Boolean = false)
    : ListAdapter<ClazzWorkWithSubmission,
        SubmissionTextEntryWithResultRecyclerAdapter.SubmissionTextEntryWithResultViewHolder>(
        ClazzWorkDetailOverviewFragment.DU_CLAZZWORKWITHSUBMISSION) {

    var visible: Boolean = visible
        set(value) {
            if(field == value)
                return

            field = value
        }

    var markingMode: Boolean = marking
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.markingMode = value
            viewHolder?.itemBinding?.freeText = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT

        }

    class SubmissionTextEntryWithResultViewHolder(
            var itemBinding: ItemClazzworkSubmissionTextEntryBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: SubmissionTextEntryWithResultViewHolder? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : SubmissionTextEntryWithResultViewHolder {
        return SubmissionTextEntryWithResultViewHolder(
                ItemClazzworkSubmissionTextEntryBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.freeText = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT
                    it.markingMode = markingMode
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun getItemCount(): Int {
        return if(visible) 1 else 0
    }

    override fun onBindViewHolder(holder: SubmissionTextEntryWithResultViewHolder, position: Int) {
        holder.itemBinding.clazzWorkWithSubmission = getItem(position)
        holder.itemView.tag = getItem(position).clazzWorkUid ?: 0L
        holder.itemBinding.markingMode = markingMode
    }
}