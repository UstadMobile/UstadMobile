package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemStudentAssignmentProgressDetailBinding
import com.ustadmobile.lib.db.entities.StudentAssignmentProgress
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter


class StudentAssignmentProgressRecyclerAdapter(studentProgress: StudentAssignmentProgress?)
    : SingleItemRecyclerViewAdapter<StudentAssignmentProgressRecyclerAdapter.StudentAssignmentProgressViewHolder>() {

    class StudentAssignmentProgressViewHolder(var itemBinding: ItemStudentAssignmentProgressDetailBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: StudentAssignmentProgressViewHolder? = null

    private var studentAssignmentProgressVal: StudentAssignmentProgress? = studentProgress
        set(value){
            field = value
            visible = value?.hasMetricsPermission ?: false
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentAssignmentProgressViewHolder {
        return StudentAssignmentProgressViewHolder(
                ItemStudentAssignmentProgressDetailBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: StudentAssignmentProgressViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemBinding.studentProgress = studentAssignmentProgressVal
    }

}