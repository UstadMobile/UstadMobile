package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemDiscussionPostDetailBinding
import com.ustadmobile.lib.db.entities.DiscussionPost
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class DiscussionPostDescriptionRecyclerAdapter
    : SingleItemRecyclerViewAdapter<DiscussionPostDescriptionRecyclerAdapter.DiscussionPostViewHolder>(true) {

    class DiscussionPostViewHolder(var itemBinding: ItemDiscussionPostDetailBinding)
        : RecyclerView.ViewHolder(itemBinding.root)


    private var viewHolder: DiscussionPostViewHolder? = null

    var discussionTopic: DiscussionPostWithDetails? = null
        set(value){
            if(field == value) return
            field = value
            viewHolder?.itemBinding?.discussionPost = value
        }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscussionPostViewHolder {
        viewHolder =  DiscussionPostViewHolder(
            ItemDiscussionPostDetailBinding.inflate(LayoutInflater.from(parent.context),
                parent, false))
        return viewHolder as DiscussionPostViewHolder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: DiscussionPostViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

    }
}