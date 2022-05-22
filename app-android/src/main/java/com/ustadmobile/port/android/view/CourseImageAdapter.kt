package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCourseImageBinding
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails

class CourseImageAdapter(

) : ListAdapter<ClazzWithDisplayDetails, CourseImageAdapter.CourseImageViewHolder>(DIFFUTIL) {

    class CourseImageViewHolder(var itemBinding: ItemCourseImageBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseImageViewHolder {
        return CourseImageViewHolder(
            ItemCourseImageBinding.inflate(LayoutInflater.from(parent.context),
                parent, false))
    }

    override fun onBindViewHolder(holder: CourseImageViewHolder, position: Int) {
        holder.itemBinding.clazz = getItem(position)
    }

    companion object {

        val DIFFUTIL = object: DiffUtil.ItemCallback<ClazzWithDisplayDetails>() {
            override fun areItemsTheSame(
                oldItem: ClazzWithDisplayDetails,
                newItem: ClazzWithDisplayDetails
            ): Boolean {
                return oldItem.clazzUid == newItem.clazzUid
            }

            override fun areContentsTheSame(
                oldItem: ClazzWithDisplayDetails,
                newItem: ClazzWithDisplayDetails
            ): Boolean {
                return oldItem.clazzUid == newItem.clazzUid
            }
        }

    }
}