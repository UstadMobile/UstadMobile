package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzworkSubmissionResultBinding
import com.ustadmobile.lib.db.entities.ClazzWorkWithSubmission

class SubmissionResultRecyclerAdapter(clazzWork: ClazzWorkWithSubmission?,
                                      visible: Boolean = false)
    : ListAdapter<ClazzWorkWithSubmission,
        SubmissionResultRecyclerAdapter.SubmissionResultViewHolder>(
            ClazzWorkDetailOverviewFragment.DU_CLAZZWORKWITHSUBMISSION) {

    var visible: Boolean = visible
        set(value) {
            if(field == value)
                return

            field = value
        }

    class SubmissionResultViewHolder(var itemBinding: ItemClazzworkSubmissionResultBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: SubmissionResultViewHolder? = null

    var _clazzWork : ClazzWorkWithSubmission? = clazzWork
        set(value){
            if(field == value)
                return
            notifyDataSetChanged()
            viewHolder?.itemBinding?.clazzWorkWithSubmission = value
            viewHolder?.itemView?.tag = value?.clazzWorkSubmission?.clazzWorkSubmissionUid?:0L
            notifyDataSetChanged()
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionResultViewHolder {
        return SubmissionResultViewHolder(
                ItemClazzworkSubmissionResultBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.clazzWorkWithSubmission = _clazzWork
                    viewHolder?.itemView?.tag = _clazzWork?.clazzWorkSubmission?.clazzWorkSubmissionUid
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun getItemCount(): Int {
        return if(visible) 1 else 0
    }

    override fun onBindViewHolder(holder: SubmissionResultViewHolder, position: Int) {
    }
}