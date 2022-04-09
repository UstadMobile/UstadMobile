package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemAssignmentFileSubmissionBottomBinding
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithCourseBlock
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class AddSubmissionButtonsAdapter(val eventHandler: ClazzAssignmentDetailOverviewFragmentEventHandler): SingleItemRecyclerViewAdapter<
        AddSubmissionButtonsAdapter.FileSubmissionBottomViewHolder>(false) {


    var maxFilesReached: Boolean = false
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.showAddFile = checkCanAddFile()
        }

    var deadlinePassed: Boolean = false
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.deadlinePassed = value
            viewHolder?.itemBinding?.showAddFile = checkCanAddFile()
        }

    var assignment: ClazzAssignmentWithCourseBlock? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.assignment = value
            viewHolder?.itemBinding?.showAddFile = checkCanAddFile()
        }


    class FileSubmissionBottomViewHolder(var itemBinding: ItemAssignmentFileSubmissionBottomBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: FileSubmissionBottomViewHolder? = null

    private fun checkCanAddFile(): Boolean {
        return (assignment?.caRequireFileSubmission ?: false) && !(maxFilesReached || deadlinePassed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileSubmissionBottomViewHolder {
        viewHolder = FileSubmissionBottomViewHolder(
                ItemAssignmentFileSubmissionBottomBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.assignment = assignment
                    it.eventHandler = eventHandler
                    it.showAddFile = checkCanAddFile()
                    it.deadlinePassed = deadlinePassed
                })
        return viewHolder as FileSubmissionBottomViewHolder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

}