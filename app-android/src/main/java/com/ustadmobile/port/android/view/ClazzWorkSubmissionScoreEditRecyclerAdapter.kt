package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzworkSubmissionScoreEditBinding
import com.ustadmobile.lib.db.entities.ClazzMemberAndClazzWorkWithSubmission
import com.ustadmobile.lib.db.entities.ClazzWork

class ClazzWorkSubmissionScoreEditRecyclerAdapter(clazzWork: ClazzMemberAndClazzWorkWithSubmission?,
                                                  visible: Boolean = false)
    : ListAdapter<ClazzMemberAndClazzWorkWithSubmission,
        ClazzWorkSubmissionScoreEditRecyclerAdapter.ScoreEditViewHolder>(
            ClazzWorkDetailOverviewFragment.DU_CLAZZMEMBERANDCLAZZWORKWITHSUBMISSION) {

    class ScoreEditViewHolder(var itemBinding: ItemClazzworkSubmissionScoreEditBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: ScoreEditViewHolder? = null
    var clazzWorkVal : ClazzMemberAndClazzWorkWithSubmission? = clazzWork

    var visible: Boolean = visible
        set(value) {
            if(field == value)
                return

            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreEditViewHolder {
        return ScoreEditViewHolder(
                ItemClazzworkSubmissionScoreEditBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.noneType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE
                    it.clazzWorkWithSubmission = clazzWorkVal
                })
    }

    override fun getItemCount(): Int {
        return if(visible) 1 else 0
    }


    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: ScoreEditViewHolder, position: Int) {

        if(currentList.size > 0){
            holder.itemBinding.clazzWorkWithSubmission = clazzWorkVal
            holder.itemView.tag = clazzWorkVal?.submission?.clazzWorkSubmissionUid?:0L
        }else {
            holder.itemBinding.clazzWorkWithSubmission = clazzWorkVal
            holder.itemView.tag = clazzWorkVal?.submission?.clazzWorkSubmissionUid?:0L
        }


    }
}