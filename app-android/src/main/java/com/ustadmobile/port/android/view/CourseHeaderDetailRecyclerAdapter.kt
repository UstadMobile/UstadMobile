package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCourseDetailHeaderOverviewBinding
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class CourseHeaderDetailRecyclerAdapter(val listener: ClazzDetailOverviewEventListener?)
    : SingleItemRecyclerViewAdapter<CourseHeaderDetailRecyclerAdapter.CourseDetailViewHolder>(true) {

    class CourseDetailViewHolder(var itemBinding: ItemCourseDetailHeaderOverviewBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: CourseDetailViewHolder? = null

    var clazz: ClazzWithDisplayDetails? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.clazz = value
        }

    var clazzCodeVisible: Boolean = false
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.clazzCodeVisible = value
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseDetailViewHolder {
        viewHolder =  CourseDetailViewHolder(
            ItemCourseDetailHeaderOverviewBinding.inflate(LayoutInflater.from(parent.context),
                parent, false).also {
                it.clazz = clazz
                it.fragmentEventHandler = listener
                it.clazzCodeVisible = clazzCodeVisible
            })
        return viewHolder as CourseDetailViewHolder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: CourseDetailViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemBinding.clazz = clazz
        holder.itemBinding.clazzCodeVisible = clazzCodeVisible
    }
}