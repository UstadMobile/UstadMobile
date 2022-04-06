
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCourseGroupSetListBinding
import com.ustadmobile.core.controller.CourseGroupSetListPresenter
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class CourseGroupSetListRecyclerAdapter(var presenter: CourseGroupSetListPresenter?): SelectablePagedListAdapter<CourseGroupSet, CourseGroupSetListRecyclerAdapter.CourseGroupSetListViewHolder>(DIFF_CALLBACK) {

    class CourseGroupSetListViewHolder(val itemBinding: ItemCourseGroupSetListBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseGroupSetListViewHolder {
        val itemBinding = ItemCourseGroupSetListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.presenter = presenter
        itemBinding.selectablePagedListAdapter = this
        return CourseGroupSetListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: CourseGroupSetListViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.courseGroupSet = item
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        presenter = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<CourseGroupSet> = object
            : DiffUtil.ItemCallback<CourseGroupSet>() {
            override fun areItemsTheSame(oldItem: CourseGroupSet,
                                         newItem: CourseGroupSet): Boolean {
                return oldItem.cgsUid == newItem.cgsUid
            }

            override fun areContentsTheSame(oldItem: CourseGroupSet,
                                            newItem: CourseGroupSet): Boolean {
                //Check only those fields that are displayed to the user to minimize refreshes
               return oldItem.cgsUid == newItem.cgsUid && oldItem.cgsName == newItem.cgsName
            }
        }
    }

}