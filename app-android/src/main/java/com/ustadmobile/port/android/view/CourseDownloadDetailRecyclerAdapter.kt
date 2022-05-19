package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCourseDetailDownloadBinding
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class CourseDownloadDetailRecyclerAdapter(val listener: ClazzDetailOverviewEventListener?)
    : SingleItemRecyclerViewAdapter<CourseDownloadDetailRecyclerAdapter.CourseDownloadDetailViewHolder>(true) {

    class CourseDownloadDetailViewHolder(var itemBinding: ItemCourseDetailDownloadBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: CourseDownloadDetailViewHolder? = null

    var permissionButtonVisible: Boolean = false
        set(value) {
            field = value
            currentViewHolder?.itemBinding?.permissionButtonVisible = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseDownloadDetailViewHolder {
        viewHolder =  CourseDownloadDetailViewHolder(
            ItemCourseDetailDownloadBinding.inflate(LayoutInflater.from(parent.context),
                parent, false).also {
                it.fragmentEventHandler = listener
            })
        return viewHolder as CourseDownloadDetailViewHolder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: CourseDownloadDetailViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemBinding?.permissionButtonVisible = permissionButtonVisible
    }
}