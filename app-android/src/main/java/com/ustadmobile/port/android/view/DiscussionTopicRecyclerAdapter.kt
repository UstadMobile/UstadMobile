package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemDiscussionTopicListDetailBinding
import com.ustadmobile.core.controller.CourseDiscussionDetailPresenter
import com.ustadmobile.lib.db.entities.DiscussionTopicListDetail
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class DiscussionTopicListDetailViewHolder(val itemBinding: ItemDiscussionTopicListDetailBinding)
    : RecyclerView.ViewHolder(itemBinding.root)

class DiscussionTopicRecyclerAdapter(var presenter: CourseDiscussionDetailPresenter?)
    : SelectablePagedListAdapter<DiscussionTopicListDetail,
        DiscussionTopicListDetailViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscussionTopicListDetailViewHolder {
        val itemBinding = ItemDiscussionTopicListDetailBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        itemBinding.mPresenter = presenter
        return DiscussionTopicListDetailViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: DiscussionTopicListDetailViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.discussionTopic = item
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        presenter = null
    }

    companion object{
        val DIFF_CALLBACK: DiffUtil.ItemCallback<DiscussionTopicListDetail> = object
            : DiffUtil.ItemCallback<DiscussionTopicListDetail>() {
            override fun areItemsTheSame(oldItem: DiscussionTopicListDetail,
                                         newItem: DiscussionTopicListDetail
            ): Boolean {

                return oldItem.discussionTopicUid == newItem.discussionTopicUid
            }

            override fun areContentsTheSame(oldItem: DiscussionTopicListDetail,
                                            newItem: DiscussionTopicListDetail
            ): Boolean {
                return oldItem.discussionTopicTitle == newItem.discussionTopicTitle &&
                        oldItem.discussionTopicArchive == newItem.discussionTopicArchive &&
                        oldItem.discussionTopicVisible == newItem.discussionTopicVisible &&
                        oldItem.numPosts == newItem.numPosts &&
                        oldItem.lastActiveTimestamp == newItem.lastActiveTimestamp
            }
        }


    }
}