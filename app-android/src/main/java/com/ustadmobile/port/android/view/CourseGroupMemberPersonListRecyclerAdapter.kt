
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCourseGroupMemberPersonBinding
import com.toughra.ustadmobile.databinding.ItemCourseGroupMemberPersonHeaderBinding
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.lib.db.entities.CourseGroupMemberPerson


class CourseGroupMemberPersonListRecyclerAdapter():
    ListAdapter<CourseGroupMemberPerson,
            RecyclerView.ViewHolder>(
        DIFF_CALLBACK
    ) {

    class CourseGroupMemberPersonListViewHolder(val itemBinding: ItemCourseGroupMemberPersonBinding): RecyclerView.ViewHolder(itemBinding.root)

    class CourseGroupMemberHeaderListViewHolder(val itemBinding: ItemCourseGroupMemberPersonHeaderBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder = when(viewType){
            TYPE_HEADER -> {
                CourseGroupMemberHeaderListViewHolder(
                    ItemCourseGroupMemberPersonHeaderBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false))
            }
            else -> {
                CourseGroupMemberPersonListViewHolder(
                    ItemCourseGroupMemberPersonBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false))
            }
        }
        return viewHolder
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position).personUid){
            0L -> TYPE_HEADER
            else -> TYPE_MEMBER
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when(holder.itemViewType){
            TYPE_HEADER -> (holder as CourseGroupMemberHeaderListViewHolder).itemBinding.groupMember = item
            else -> (holder as CourseGroupMemberPersonListViewHolder).itemBinding.groupMember = item
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

    }

    companion object {

        private const val TYPE_HEADER = 1
        private const val TYPE_MEMBER = 2

        val DIFF_CALLBACK: DiffUtil.ItemCallback<CourseGroupMemberPerson> = object
            : DiffUtil.ItemCallback<CourseGroupMemberPerson>() {
            override fun areItemsTheSame(oldItem: CourseGroupMemberPerson,
                                         newItem: CourseGroupMemberPerson): Boolean {
                return oldItem.personUid == newItem.personUid &&
                        oldItem.member?.cgmUid == newItem.member?.cgmUid &&
                        oldItem.member?.cgmGroupNumber == newItem.member?.cgmGroupNumber
            }

            override fun areContentsTheSame(oldItem: CourseGroupMemberPerson,
                                            newItem: CourseGroupMemberPerson): Boolean {
                return oldItem.personUid == newItem.personUid &&
                        oldItem.personFullName() == newItem.personFullName() &&
                        oldItem.member?.cgmUid == newItem.member?.cgmUid &&
                        oldItem.member?.cgmGroupNumber == newItem.member?.cgmGroupNumber
            }
        }
    }

}