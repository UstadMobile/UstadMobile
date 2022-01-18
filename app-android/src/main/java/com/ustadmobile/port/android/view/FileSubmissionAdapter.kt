package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemAssignmentFileSubmissionBinding
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.attachments.retrieveAttachment
import com.ustadmobile.lib.db.entities.AssignmentFileSubmission
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.on

class FileSubmissionAdapter(
        val eventHandler: ClazzAssignmentDetailOverviewFragmentEventHandler)
    : SelectablePagedListAdapter<AssignmentFileSubmission,
        FileSubmissionAdapter.FileSubmissionViewHolder>(DIFF_CALLBACK_FILE_SUBMISSION){


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


    class FileSubmissionViewHolder(val binding: ItemAssignmentFileSubmissionBinding)
        : RecyclerView.ViewHolder(binding.root)

    private var viewHolder: FileSubmissionViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileSubmissionViewHolder {
        viewHolder = FileSubmissionViewHolder(
                ItemAssignmentFileSubmissionBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.assignment = assignment
                    it.eventHandler = eventHandler
                    it.showFiles = visible
                    it.hasPassedDeadline = hasPassedDeadline
                })
        return viewHolder as FileSubmissionViewHolder
    }

    override fun onBindViewHolder(holder: FileSubmissionViewHolder, position: Int) {
        holder.binding.fileSubmission = getItem(position)
    }

    companion object{

        val DIFF_CALLBACK_FILE_SUBMISSION =
                object : DiffUtil.ItemCallback<AssignmentFileSubmission>() {
                    override fun areItemsTheSame(oldItem: AssignmentFileSubmission,
                                                 newItem: AssignmentFileSubmission): Boolean {
                        return oldItem.afsUid == newItem.afsUid
                    }

                    override fun areContentsTheSame(oldItem: AssignmentFileSubmission,
                                                    newItem: AssignmentFileSubmission): Boolean {
                        return oldItem == newItem
                    }
                }


    }

}