package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemAssignmentFileSubmissionBottomBinding
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithCourseBlock
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class AddSubmissionButtonsAdapter(val eventHandler: ClazzAssignmentDetailOverviewFragmentEventHandler): SingleItemRecyclerViewAdapter<
        AddSubmissionButtonsAdapter.FileSubmissionBottomViewHolder>(false) {

    var addFileVisible: Boolean = false
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.addFileVisible = value
        }

    var addTextVisible: Boolean = false
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.addTextVisible = value
        }

    var assignment: ClazzAssignmentWithCourseBlock? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.assignment = value
        }


    class FileSubmissionBottomViewHolder(var itemBinding: ItemAssignmentFileSubmissionBottomBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: FileSubmissionBottomViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileSubmissionBottomViewHolder {
        viewHolder = FileSubmissionBottomViewHolder(
                ItemAssignmentFileSubmissionBottomBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.assignment = assignment
                    it.eventHandler = eventHandler
                    it.addFileVisible = addFileVisible
                    it.addTextVisible = addTextVisible
                })
        return viewHolder as FileSubmissionBottomViewHolder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

}