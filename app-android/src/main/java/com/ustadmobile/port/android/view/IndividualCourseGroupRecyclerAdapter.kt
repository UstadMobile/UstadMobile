
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCourseGroupSetListBinding
import com.ustadmobile.core.controller.CourseGroupSetListPresenter
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.port.android.view.CourseGroupSetListRecyclerAdapter.Companion.DIFF_CALLBACK


class IndividualCourseGroupRecyclerAdapter(
    var presenter: CourseGroupSetListPresenter?
    ): ListAdapter<CourseGroupSet,
        CourseGroupSetListRecyclerAdapter.CourseGroupSetListViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseGroupSetListRecyclerAdapter.CourseGroupSetListViewHolder {
        val itemBinding = ItemCourseGroupSetListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.presenter = presenter
        return CourseGroupSetListRecyclerAdapter.CourseGroupSetListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: CourseGroupSetListRecyclerAdapter.CourseGroupSetListViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.courseGroupSet = item
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        presenter = null
    }

}