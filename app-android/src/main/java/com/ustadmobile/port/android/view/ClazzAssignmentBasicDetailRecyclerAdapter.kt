package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzAssignmentDetailBinding
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class ClazzAssignmentBasicDetailRecyclerAdapter()
    : SingleItemRecyclerViewAdapter<ClazzAssignmentBasicDetailRecyclerAdapter.ClazzAssignmentDetailViewHolder>() {

    class ClazzAssignmentDetailViewHolder(var itemBinding: ItemClazzAssignmentDetailBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: ClazzAssignmentDetailViewHolder? = null

    var clazzAssignment: ClazzAssignment? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.clazzAssignment = value
            viewHolder?.itemView?.tag = value?.caUid?:0L
        }

    var timeZone: String? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.timeZone = value
        }

    var submissionMark: CourseAssignmentMark? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.submissionStatus = value
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzAssignmentDetailViewHolder {
        return ClazzAssignmentDetailViewHolder(
                ItemClazzAssignmentDetailBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.clazzAssignment = clazzAssignment
                    viewHolder?.itemView?.tag = clazzAssignment?.caUid?:0L
                    it.timeZone = timeZone
                    it.submissionStatus = submissionMark
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: ClazzAssignmentDetailViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        viewHolder?.itemView?.tag = clazzAssignment?.caUid?:0L
        viewHolder?.itemBinding?.itemClazzAssignmetnDetailDescriptionCl?.tag =
                clazzAssignment?.caUid?:0L
        viewHolder?.itemBinding?.timeZone = timeZone
        viewHolder?.itemBinding?.submissionStatus = submissionMark
    }
}