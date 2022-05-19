
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCourseTerminologyListBinding
import com.ustadmobile.core.controller.CourseTerminologyListItemListener
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class CourseTerminologyListRecyclerAdapter(
    var itemListener: CourseTerminologyListItemListener?):
    SelectablePagedListAdapter<CourseTerminology,
            CourseTerminologyListRecyclerAdapter.CourseTerminologyListViewHolder>(DIFF_CALLBACK) {

    class CourseTerminologyListViewHolder(val itemBinding: ItemCourseTerminologyListBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseTerminologyListViewHolder {
        val itemBinding = ItemCourseTerminologyListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.itemListener = itemListener
        itemBinding.selectablePagedListAdapter = this
        return CourseTerminologyListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: CourseTerminologyListViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.courseTerminology = item
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<CourseTerminology> = object
            : DiffUtil.ItemCallback<CourseTerminology>() {
            override fun areItemsTheSame(oldItem: CourseTerminology,
                                         newItem: CourseTerminology): Boolean {
                return oldItem.ctUid == newItem.ctUid
            }

            override fun areContentsTheSame(oldItem: CourseTerminology,
                                            newItem: CourseTerminology): Boolean {
                return oldItem.ctUid == newItem.ctUid && oldItem.ctTitle == newItem.ctTitle
            }
        }
    }

}