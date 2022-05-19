package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemMarkFileSubmissionBinding
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithCourseBlock
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class MarkFileSubmissionAdapter(val eventHandler: ClazzAssignmentDetailStudentProgressFragmentEventHandler): SingleItemRecyclerViewAdapter<
        MarkFileSubmissionAdapter.MarkFileSubmissionViewHolder>(true) {

    class MarkFileSubmissionViewHolder(var itemBinding: ItemMarkFileSubmissionBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: MarkFileSubmissionViewHolder? = null

    var assignment: ClazzAssignmentWithCourseBlock? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.assignment = value
        }

    var mark: CourseAssignmentMark? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.mark = value
        }

    var submitMarkError: String? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.errorText = value
        }

    var markStudentVisible = false
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.markStudentVisible = value
        }


    var markNextStudentVisible = false
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.markNextStudentVisible = value
        }

    var grade: Float? = null
        get() = viewHolder?.itemBinding?.markFileSubmissionTextInput?.editText?.text.toString().toFloatOrNull()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkFileSubmissionViewHolder {
        viewHolder = MarkFileSubmissionViewHolder(
                ItemMarkFileSubmissionBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false)).also {
            it.itemBinding.errorText = submitMarkError
            it.itemBinding.eventHandler = eventHandler
            it.itemBinding.assignment = assignment
            it.itemBinding.mark = mark
            it.itemBinding.markNextStudentVisible = markNextStudentVisible
            it.itemBinding.markStudentVisible = markStudentVisible
        }
        return viewHolder as MarkFileSubmissionViewHolder
    }


    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }


}