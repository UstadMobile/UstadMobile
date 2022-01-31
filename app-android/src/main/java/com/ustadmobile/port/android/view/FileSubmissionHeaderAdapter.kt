package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemAssignmentFileSubmissionHeaderBinding
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class FileSubmissionHeaderAdapter(heading: String): SingleItemRecyclerViewAdapter<
        FileSubmissionHeaderAdapter.FileSubmissionHeaderViewHolder>() {

    var headingText: String? = heading
        set(value) {
            field = value
            viewHolder?.itemBinding?.headingText = value
            viewHolder?.itemView?.tag = headingText
        }

    var assignment: ClazzAssignment? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.assignment = value
        }

    var fileSubmissionScore: ContentEntryStatementScoreProgress? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.fileSubmissionScore = value
        }


    class FileSubmissionHeaderViewHolder(var itemBinding: ItemAssignmentFileSubmissionHeaderBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: FileSubmissionHeaderViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileSubmissionHeaderViewHolder {
        viewHolder = FileSubmissionHeaderViewHolder(
                ItemAssignmentFileSubmissionHeaderBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.headingText = headingText
                    it.assignment = assignment
                    it.headingText = headingText
                    it.fileSubmissionScore = fileSubmissionScore
                })
        return viewHolder as FileSubmissionHeaderViewHolder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

}