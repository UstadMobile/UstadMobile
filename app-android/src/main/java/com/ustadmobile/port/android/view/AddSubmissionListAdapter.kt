package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemAssignmentAddSubmissionBinding
import com.ustadmobile.core.controller.FileSubmissionListItemListener
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionWithAttachment
import com.ustadmobile.port.android.view.binding.MODE_START_OF_DAY

class AddSubmissionListAdapter(
        var itemListener: FileSubmissionListItemListener?)
    : ListAdapter<CourseAssignmentSubmissionWithAttachment,
        AddSubmissionListAdapter.AddSubmissionViewHolder>(DIFF_CALLBACK_FILE_SUBMISSION){


    var visible: Boolean = false
        set(value) {
            if(field == value)
                return
            field = value
            viewHolder?.binding?.showFiles = value
        }


    var assignment: ClazzAssignment? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.binding?.assignment = value
        }

    var hasPassedDeadline: Boolean = false
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.binding?.hasPassedDeadline = value
        }

    var isSubmitted: Boolean = false
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.binding?.notSubmitted = !value
        }

    private var viewHolder: AddSubmissionViewHolder? = null

    class AddSubmissionViewHolder(val binding: ItemAssignmentAddSubmissionBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddSubmissionViewHolder {
        viewHolder = AddSubmissionViewHolder(
                ItemAssignmentAddSubmissionBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.assignment = assignment
                    it.eventHandler = itemListener
                    it.showFiles = visible
                    it.notSubmitted = !isSubmitted
                    it.hasPassedDeadline = hasPassedDeadline
                    it.dateTimeMode = MODE_START_OF_DAY
                    it.timeZoneId = "UTC"
                })
        return viewHolder as AddSubmissionViewHolder
    }

    override fun onBindViewHolder(holder: AddSubmissionViewHolder, position: Int) {
        val item =  getItem(position)
        holder.binding.fileSubmission = item
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
        viewHolder = null
    }

    companion object{

        val DIFF_CALLBACK_FILE_SUBMISSION =
                object : DiffUtil.ItemCallback<CourseAssignmentSubmissionWithAttachment>() {
                    override fun areItemsTheSame(oldItem: CourseAssignmentSubmissionWithAttachment,
                                                 newItem: CourseAssignmentSubmissionWithAttachment): Boolean {
                        return oldItem.casUid == newItem.casUid
                    }

                    override fun areContentsTheSame(oldItem: CourseAssignmentSubmissionWithAttachment,
                                                    newItem: CourseAssignmentSubmissionWithAttachment): Boolean {
                        return oldItem.casUid == newItem.casUid
                    }
                }


    }

}