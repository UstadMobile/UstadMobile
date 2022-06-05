package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemDiscussionPostListDetailBinding
import com.ustadmobile.core.controller.DiscussionTopicDetailPresenter
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class DiscussionPostListDetailViewHolder(val itemBinding: ItemDiscussionPostListDetailBinding)
    : RecyclerView.ViewHolder(itemBinding.root)

class DiscussionPostRecyclerAdapter(var presenter: DiscussionTopicDetailPresenter?)
    : SelectablePagedListAdapter<DiscussionPostWithDetails,
        DiscussionPostListDetailViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscussionPostListDetailViewHolder {
        val itemBinding = ItemDiscussionPostListDetailBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        itemBinding.mPresenter = presenter
        return DiscussionPostListDetailViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: DiscussionPostListDetailViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.discussionPost = item
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        presenter = null
    }

    companion object{
        val DIFF_CALLBACK: DiffUtil.ItemCallback<DiscussionPostWithDetails> = object
            : DiffUtil.ItemCallback<DiscussionPostWithDetails>() {
            override fun areItemsTheSame(oldItem: DiscussionPostWithDetails,
                                         newItem: DiscussionPostWithDetails
            ): Boolean {

                return oldItem.discussionPostUid == newItem.discussionPostUid
            }

            override fun areContentsTheSame(oldItem: DiscussionPostWithDetails,
                                            newItem: DiscussionPostWithDetails
            ): Boolean {
                return oldItem.discussionPostTitle == newItem.discussionPostTitle &&
                        oldItem.discussionPostMessage == newItem.discussionPostMessage &&
                        oldItem.discussionPostVisible == newItem.discussionPostVisible &&
                        oldItem.discussionPostArchive == newItem.discussionPostArchive &&
                        oldItem.discussionPostStartedPersonUid == newItem.discussionPostStartedPersonUid &&
                        oldItem.authorPersonFirstNames == newItem.authorPersonFirstNames &&
                        oldItem.authorPersonLastName == newItem.authorPersonLastName &&
                        oldItem.postLatestMessage == newItem.postLatestMessage &&
                        oldItem.postLatestMessageTimestamp == newItem.postLatestMessageTimestamp &&
                        oldItem.postRepliesCount == newItem.postRepliesCount
            }
        }


    }
}