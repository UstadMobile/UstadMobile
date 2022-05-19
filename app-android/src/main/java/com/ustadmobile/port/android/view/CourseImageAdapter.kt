package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCourseImageBinding
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class CourseImageAdapter()
    : SingleItemRecyclerViewAdapter<CourseImageAdapter.CourseImageViewHolder>(true) {

    class CourseImageViewHolder(var itemBinding: ItemCourseImageBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: CourseImageViewHolder? = null

    var clazz: ClazzWithDisplayDetails? = null
        set(value) {
            if (field == value)
                return
            field = value
            viewHolder?.itemBinding?.clazz = value
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseImageViewHolder {
        viewHolder =  CourseImageViewHolder(
            ItemCourseImageBinding.inflate(LayoutInflater.from(parent.context),
                parent, false))
        return viewHolder as CourseImageViewHolder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: CourseImageViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
    }
}