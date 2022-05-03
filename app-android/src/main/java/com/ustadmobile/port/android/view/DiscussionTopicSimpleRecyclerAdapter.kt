package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemDiscussionTopicSimpleBinding
import com.ustadmobile.core.controller.CourseDiscussionEditPresenter
import com.ustadmobile.lib.db.entities.DiscussionTopic
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class DiscussionTopicSimpleDetailViewHolder(val itemBinding: ItemDiscussionTopicSimpleBinding)
    : RecyclerView.ViewHolder(itemBinding.root)

class DiscussionTopicSimpleRecyclerAdapter(var presenter: CourseDiscussionEditPresenter?)
    : ListAdapter<DiscussionTopic,
        DiscussionTopicSimpleDetailViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscussionTopicSimpleDetailViewHolder {
        val itemBinding = ItemDiscussionTopicSimpleBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        itemBinding.mPresenter = presenter
        return DiscussionTopicSimpleDetailViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: DiscussionTopicSimpleDetailViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.discussionTopic = item
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        presenter = null
    }

    companion object{
        val DIFF_CALLBACK: DiffUtil.ItemCallback<DiscussionTopic> = object
            : DiffUtil.ItemCallback<DiscussionTopic>() {
            override fun areItemsTheSame(oldItem: DiscussionTopic,
                                         newItem: DiscussionTopic
            ): Boolean {

                return oldItem.discussionTopicUid == newItem.discussionTopicUid
            }

            override fun areContentsTheSame(oldItem: DiscussionTopic,
                                            newItem: DiscussionTopic
            ): Boolean {
                return oldItem.discussionTopicTitle == newItem.discussionTopicTitle &&
                        oldItem.discussionTopicArchive == newItem.discussionTopicArchive &&
                        oldItem.discussionTopicVisible == newItem.discussionTopicVisible
            }
        }


    }
}