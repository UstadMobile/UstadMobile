package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzworkSubmissionShortTextEntryBinding
import com.ustadmobile.lib.db.entities.ClazzEnrollmentAndClazzWorkWithSubmission
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class ClazzWorkShortTextResultRecyclerAdapter(clazzWorkWithSubmission: ClazzEnrollmentAndClazzWorkWithSubmission?)
    : SingleItemRecyclerViewAdapter<
        ClazzWorkShortTextResultRecyclerAdapter.ClazzWorkShortTextSubmissionViewHolder>() {


    var clazzWorkWithSubmission: ClazzEnrollmentAndClazzWorkWithSubmission? = clazzWorkWithSubmission
        set(value){
            if(field == value){
                return
            }
            field = value
            viewHolder?.itemBinding?.clazzWorkWithSubmission = value
            viewHolder?.itemBinding?.freeText = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT
            viewHolder?.itemBinding?.showEdit = showSubmissionEdit
        }

    var showSubmissionEdit : Boolean = false
        set(value){
            field = value
            viewHolder?.itemBinding?.showEdit = value
        }



    class ClazzWorkShortTextSubmissionViewHolder(
            var itemBinding: ItemClazzworkSubmissionShortTextEntryBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: ClazzWorkShortTextSubmissionViewHolder? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : ClazzWorkShortTextSubmissionViewHolder {
        return ClazzWorkShortTextSubmissionViewHolder(
                ItemClazzworkSubmissionShortTextEntryBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.freeText = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT
                    it.clazzWorkWithSubmission = clazzWorkWithSubmission
                    it.showEdit = showSubmissionEdit
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }


    override fun onBindViewHolder(holder: ClazzWorkShortTextSubmissionViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemBinding.clazzWorkWithSubmission = clazzWorkWithSubmission
        holder.itemView.tag = clazzWorkWithSubmission?.clazzWork?.clazzWorkUid?:0L
        holder.itemBinding.freeText = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT
        viewHolder?.itemBinding?.showEdit = showSubmissionEdit
    }
}