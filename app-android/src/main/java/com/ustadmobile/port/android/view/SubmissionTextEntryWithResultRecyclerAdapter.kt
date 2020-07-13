package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzworkSubmissionTextEntryBinding
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.ClazzWorkWithSubmission

class SubmissionTextEntryWithResultRecyclerAdapter(clazzWork: ClazzWorkWithSubmission?,
                                                   visible: Boolean = false, editMode: Boolean = true)
    : ListAdapter<ClazzWorkWithSubmission,
        SubmissionTextEntryWithResultRecyclerAdapter.SubmissionTextEntryWithResultViewHolder>(
        ClazzWorkDetailOverviewFragment.DU_CLAZZWORKWITHSUBMISSION) {

    var visible: Boolean = visible
        set(value) {
            if(field == value)
                return

            field = value
        }

    var modeEdit: Boolean = editMode
        set(value){
            field = value
            viewHolder?.itemBinding?.editMode = value
        }

    class SubmissionTextEntryWithResultViewHolder(var itemBinding: ItemClazzworkSubmissionTextEntryBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: SubmissionTextEntryWithResultViewHolder? = null

    public var _clazzWork : ClazzWorkWithSubmission? = clazzWork
        get() = field
        set(value){
            if(field == value)
                return
            notifyDataSetChanged()
            viewHolder?.itemBinding?.clazzWorkWithSubmission = value
            viewHolder?.itemView?.tag = value?.clazzWorkUid?:0L
            notifyDataSetChanged()
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionTextEntryWithResultViewHolder {
        return SubmissionTextEntryWithResultViewHolder(
                ItemClazzworkSubmissionTextEntryBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.clazzWorkWithSubmission = _clazzWork
                    it.freeText = ClazzWork.CLAZZ_WORK_SUBMISSION_TYPE_SHORT_TEXT
                    it.editMode = modeEdit
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
    }
}