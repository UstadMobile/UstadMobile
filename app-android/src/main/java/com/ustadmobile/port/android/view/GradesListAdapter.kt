package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemMarksPersonBinding
import com.ustadmobile.lib.db.entities.CourseAssignmentMarkWithPersonMarker
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class GradesListAdapter : SelectablePagedListAdapter<CourseAssignmentMarkWithPersonMarker,
        GradesListAdapter.MarksViewHolder>(DIFF_CALLBACK_MARKS) {

    var courseblock: CourseBlock? = null
        set(value){
            field = value
            viewHolder?.binding?.block = courseblock
        }

    class MarksViewHolder(val binding: ItemMarksPersonBinding)
        : RecyclerView.ViewHolder(binding.root)


    var viewHolder: MarksViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarksViewHolder {
        viewHolder =  MarksViewHolder(
            ItemMarksPersonBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ).also {
                it.block = courseblock
            }
        )
        return viewHolder as MarksViewHolder
    }

    override fun onBindViewHolder(holder: MarksViewHolder, position: Int) {
        holder.binding.markWithPerson = getItem(position)
        holder.binding.block = courseblock
    }


    companion object {


        val DIFF_CALLBACK_MARKS =
            object : DiffUtil.ItemCallback<CourseAssignmentMarkWithPersonMarker>() {
                override fun areItemsTheSame(oldItem: CourseAssignmentMarkWithPersonMarker,
                                             newItem: CourseAssignmentMarkWithPersonMarker): Boolean {
                    return oldItem.camUid == newItem.camUid && oldItem.camMarkerPersonUid == newItem.camMarkerPersonUid
                }

                override fun areContentsTheSame(oldItem: CourseAssignmentMarkWithPersonMarker,
                                                newItem: CourseAssignmentMarkWithPersonMarker): Boolean {
                    return oldItem.camMark == newItem.camMark &&
                            oldItem.camMarkerComment == newItem.camMarkerComment &&
                            oldItem.camPenalty == newItem.camPenalty &&
                            oldItem.marker?.fullName() == newItem.marker?.fullName() &&
                            oldItem.camMarkerSubmitterUid == newItem.camMarkerSubmitterUid
                }
            }
    }


}