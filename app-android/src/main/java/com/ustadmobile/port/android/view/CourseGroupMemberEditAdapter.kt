package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCourseGroupMemberEditBinding
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.lib.db.entities.CourseGroupMemberPerson

class CourseGroupMemberEditAdapter(val eventHandler: CourseGroupSetEditFragmentEventHandler) : ListAdapter<CourseGroupMemberPerson,
        CourseGroupMemberEditAdapter.GroupSetMemberAdapterHolder>(DIFF_CALLBACK) {

    private var viewHolder: GroupSetMemberAdapterHolder? = null

    var groupList: List<IdOption>? = null
        set(value){
            if((value?.size == 0) || ((value?.size ?: 0) == (field?.size ?: 0)))
                return
            field = value
            viewHolder?.itemBinding?.groupList = value
            notifyDataSetChanged()
        }


    class GroupSetMemberAdapterHolder(val itemBinding: ItemCourseGroupMemberEditBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupSetMemberAdapterHolder {
        viewHolder = GroupSetMemberAdapterHolder(ItemCourseGroupMemberEditBinding.inflate(LayoutInflater.from(parent.context), parent,
            false).also {
            it.groupList = groupList
        })
        return viewHolder as GroupSetMemberAdapterHolder
    }

    override fun onBindViewHolder(holder: GroupSetMemberAdapterHolder, position: Int) {
        holder.itemBinding.groupMember = getItem(position)
        holder.itemBinding.groupList = groupList
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
        groupList = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<CourseGroupMemberPerson> = object
            : DiffUtil.ItemCallback<CourseGroupMemberPerson>() {
            override fun areItemsTheSame(oldItem: CourseGroupMemberPerson,
                                         newItem: CourseGroupMemberPerson): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: CourseGroupMemberPerson,
                                            newItem: CourseGroupMemberPerson): Boolean {
                return oldItem === newItem
            }
        }
    }

}