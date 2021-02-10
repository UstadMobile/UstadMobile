package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzListBinding
import com.ustadmobile.core.controller.ClazzListItemListener
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class ClazzListRecyclerAdapter(var itemListener: ClazzListItemListener?)
    : SelectablePagedListAdapter<ClazzWithListDisplayDetails,
        ClazzListRecyclerAdapter.ClazzList2ViewHolder>(DIFF_CALLBACK) {

    class ClazzList2ViewHolder(val itemBinding: ItemClazzListBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzList2ViewHolder {
        val itemBinding = ItemClazzListBinding.inflate(LayoutInflater.from(parent.context), parent,
                false)
        return ClazzList2ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ClazzList2ViewHolder, position: Int) {
        holder.itemBinding.clazz = getItem(position)
        holder.itemView.tag = holder.itemBinding.clazz?.clazzUid
        holder.itemBinding.itemListener = itemListener
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzWithListDisplayDetails> = object
            : DiffUtil.ItemCallback<ClazzWithListDisplayDetails>() {
            override fun areItemsTheSame(oldItem: ClazzWithListDisplayDetails,
                                         newItem: ClazzWithListDisplayDetails): Boolean {
                return oldItem.clazzUid == newItem.clazzUid
            }

            override fun areContentsTheSame(oldItem: ClazzWithListDisplayDetails,
                                            newItem: ClazzWithListDisplayDetails): Boolean {
                return oldItem.clazzName == newItem.clazzName &&
                        oldItem.numStudents == newItem.numStudents &&
                        oldItem.numTeachers == newItem.numTeachers &&
                        oldItem.clazzDesc == newItem.clazzDesc &&
                        oldItem.clazzActiveEnrollment?.clazzEnrollmentRole == newItem.clazzActiveEnrollment?.clazzEnrollmentRole &&
                        oldItem.attendanceAverage == newItem.attendanceAverage
            }
        }
    }

}