package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemSimpleHeadingBinding
import com.ustadmobile.lib.db.entities.DiscussionTopic
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class DiscussionTopicDescriptionRecyclerAdapter
    : SingleItemRecyclerViewAdapter<DiscussionTopicDescriptionRecyclerAdapter.DiscussionTopicViewHolder>(true) {

    class DiscussionTopicViewHolder(var itemBinding: ItemSimpleHeadingBinding)
        : RecyclerView.ViewHolder(itemBinding.root)


    private var viewHolder: DiscussionTopicViewHolder? = null

    var discussionTopic: DiscussionTopic? = null
        set(value){
            if(field == value) return
            field = value
            viewHolder?.itemBinding?.headingText = value?.discussionTopicDesc?:""
        }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscussionTopicViewHolder {
        viewHolder =  DiscussionTopicViewHolder(
            ItemSimpleHeadingBinding.inflate(LayoutInflater.from(parent.context),
                parent, false))
        return viewHolder as DiscussionTopicViewHolder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: DiscussionTopicViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

    }
}