package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemAssignmentFileSubmissionHeaderBinding
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class SubmissionStatusHeaderAdapter(): SingleItemRecyclerViewAdapter<
        SubmissionStatusHeaderAdapter.FileSubmissionHeaderViewHolder>(false) {

    var assignment: ClazzAssignment? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.assignment = value
        }

    var courseAssignmentMark: CourseAssignmentMark? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.assignmentMark = value
            viewHolder?.itemBinding?.showPoints = value != null
        }

    var assignmentStatus: Int = 0
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.assignmentStatus = value
        }


    class FileSubmissionHeaderViewHolder(var itemBinding: ItemAssignmentFileSubmissionHeaderBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: FileSubmissionHeaderViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileSubmissionHeaderViewHolder {
        viewHolder = FileSubmissionHeaderViewHolder(
                ItemAssignmentFileSubmissionHeaderBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.assignment = assignment
                    it.assignmentMark = courseAssignmentMark
                    it.showPoints = courseAssignmentMark != null
                    it.assignmentStatus = assignmentStatus
                })
        return viewHolder as FileSubmissionHeaderViewHolder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

}