package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzworkSubmissionScoreEditBinding
import com.ustadmobile.lib.db.entities.ClazzWorkWithSubmission

class ClazzWorkSubmissionScoreEditRecyclerAdapter(clazzWork: ClazzWorkWithSubmission?,
                                      visible: Boolean = false)
    : ListAdapter<ClazzWorkWithSubmission,
        ClazzWorkSubmissionScoreEditRecyclerAdapter.ScoreEditViewHolder>(
            ClazzWorkDetailOverviewFragment.DU_CLAZZWORKWITHSUBMISSION) {

    var visible: Boolean = visible
        set(value) {
            if(field == value)
                return

            field = value
        }

    class ScoreEditViewHolder(var itemBinding: ItemClazzworkSubmissionScoreEditBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: ScoreEditViewHolder? = null
    private var clazzWorkVal : ClazzWorkWithSubmission? = clazzWork

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreEditViewHolder {
        return ScoreEditViewHolder(
                ItemClazzworkSubmissionScoreEditBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun getItemCount(): Int {
        return if(visible) 1 else 0
    }

    override fun onBindViewHolder(holder: ScoreEditViewHolder, position: Int) {

        if(currentList.size > 0){
            holder.itemBinding.clazzWorkWithSubmission = getItem(0)
        }else {
            holder.itemBinding.clazzWorkWithSubmission = clazzWorkVal
        }
    }
}