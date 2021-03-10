package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzworkSubmissionScoreEditBinding
import com.ustadmobile.lib.db.entities.PersonWithClazzWorkAndSubmission
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class ClazzWorkSubmissionScoreEditRecyclerAdapter(clazzWork: PersonWithClazzWorkAndSubmission?)
    : SingleItemRecyclerViewAdapter<ClazzWorkSubmissionScoreEditRecyclerAdapter.ScoreEditViewHolder>() {

    class ScoreEditViewHolder(var itemBinding: ItemClazzworkSubmissionScoreEditBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: ScoreEditViewHolder? = null

    var clazzWorkVal : PersonWithClazzWorkAndSubmission? = clazzWork
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.clazzWorkWithSubmission = value
            viewHolder?.itemBinding?.noneType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE

        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreEditViewHolder {
        return ScoreEditViewHolder(
                ItemClazzworkSubmissionScoreEditBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.noneType = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_NONE
                    it.clazzWorkWithSubmission = clazzWorkVal
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: ScoreEditViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemBinding.clazzWorkWithSubmission = clazzWorkVal
        holder.itemView.tag = clazzWorkVal?.submission?.clazzWorkSubmissionUid?:0L



    }
}