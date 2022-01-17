package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemAssignmentFileSubmissionBottomBinding
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class FileSubmissionBottomAdapter(val eventHandler: ClazzAssignmentDetailOverviewFragmentEventHandler): SingleItemRecyclerViewAdapter<
        FileSubmissionBottomAdapter.FileSubmissionBottomViewHolder>() {


    var maxFilesReached: Boolean = false
        set(value){
            field = value
            viewHolder?.itemBinding?.maxFilesReached = value
        }

    var showAddFileButton: Boolean = false
        set(value){
            field = value
            viewHolder?.itemBinding?.showAddFileButton = value
        }

    var showSubmitButton: Boolean = false
        set(value){
            field = value
            viewHolder?.itemBinding?.showSubmitButton = value
        }

    var assignment: ClazzAssignment? = null
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
        return FileSubmissionBottomViewHolder(
                ItemAssignmentFileSubmissionBottomBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.assignment = assignment
                    it.eventHandler = eventHandler
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

}