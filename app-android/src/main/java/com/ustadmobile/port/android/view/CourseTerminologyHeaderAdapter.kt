package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCourseTerminologyEditHeaderBinding
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class CourseTerminologyHeaderAdapter()
    : SingleItemRecyclerViewAdapter<CourseTerminologyHeaderAdapter.CourseTerminologyHeaderHolder>(true) {

    class CourseTerminologyHeaderHolder(var itemBinding: ItemCourseTerminologyEditHeaderBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: CourseTerminologyHeaderHolder? = null

    var courseTerminology: CourseTerminology? = null
        get() = viewHolder?.itemBinding?.courseTerminology
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.courseTerminology = value
        }

    var titleErrorText: String? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.titleErrorText = value
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseTerminologyHeaderHolder {
        viewHolder = CourseTerminologyHeaderHolder(
            ItemCourseTerminologyEditHeaderBinding.inflate(LayoutInflater.from(parent.context),
                parent, false).also {
                    it.courseTerminology = courseTerminology
                    it.titleErrorText = titleErrorText
            })
        return viewHolder as CourseTerminologyHeaderHolder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: CourseTerminologyHeaderHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemBinding.courseTerminology = courseTerminology
        holder.itemBinding.titleErrorText = titleErrorText
    }
}