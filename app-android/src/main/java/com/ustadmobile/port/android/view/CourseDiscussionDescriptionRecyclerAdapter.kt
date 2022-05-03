package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemSimpleHeadingBinding
import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class CourseDiscussionDescriptionRecyclerAdapter
    : SingleItemRecyclerViewAdapter<CourseDiscussionDescriptionRecyclerAdapter.CourseDiscussionViewHolder>(true) {

    class CourseDiscussionViewHolder(var itemBinding: ItemSimpleHeadingBinding)
        : RecyclerView.ViewHolder(itemBinding.root)


    private var viewHolder: CourseDiscussionViewHolder? = null

    var courseDiscussion: CourseDiscussion? = null
        set(value){
            if(field == value) return
            field = value
            viewHolder?.itemBinding?.headingText = value?.courseDiscussionDesc?:""
        }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseDiscussionViewHolder {
        viewHolder =  CourseDiscussionViewHolder(
            ItemSimpleHeadingBinding.inflate(LayoutInflater.from(parent.context),
                parent, false))
        return viewHolder as CourseDiscussionViewHolder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: CourseDiscussionViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

    }
}