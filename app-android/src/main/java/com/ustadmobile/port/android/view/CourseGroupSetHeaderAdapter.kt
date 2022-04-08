package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCourseGroupsetEditHeaderBinding
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class CourseGroupSetHeaderAdapter(val eventHandler: CourseGroupSetEditFragmentEventHandler)
    : SingleItemRecyclerViewAdapter<CourseGroupSetHeaderAdapter.CourseGroupSetHeaderHolder>(true) {

    class CourseGroupSetHeaderHolder(var itemBinding: ItemCourseGroupsetEditHeaderBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: CourseGroupSetHeaderHolder? = null

    var courseGroupSet: CourseGroupSet? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.courseGroupSet = value
        }

    var titleErrorText: String? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.titleErrorText = value
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseGroupSetHeaderHolder {
        viewHolder = CourseGroupSetHeaderHolder(
            ItemCourseGroupsetEditHeaderBinding.inflate(LayoutInflater.from(parent.context),
                parent, false).also {
                it.listener = eventHandler
                it.courseGroupSet = courseGroupSet
                it.titleErrorText = titleErrorText
            })
        return viewHolder as CourseGroupSetHeaderHolder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: CourseGroupSetHeaderHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemBinding.courseGroupSet = courseGroupSet
        holder.itemBinding.titleErrorText = titleErrorText
    }
}