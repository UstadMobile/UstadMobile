package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCommetsListBinding
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class CommentsRecyclerAdapter()
    : SelectablePagedListAdapter<CommentsWithPerson,
        CommentsRecyclerAdapter.CommentsWithPersonViewHolder>(DIFF_CALLBACK_COMMENTS) {

    class CommentsWithPersonViewHolder(val binding: ItemCommetsListBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsWithPersonViewHolder {
        return CommentsWithPersonViewHolder(ItemCommetsListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: CommentsWithPersonViewHolder, position: Int) {
        if(itemCount > 0 ) {
            holder.binding.commentwithperson = getItem(position)
            holder.itemView.tag = getItem(position)?.commentsUid
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
    }

    companion object{

        val DIFF_CALLBACK_COMMENTS =
                object : DiffUtil.ItemCallback<CommentsWithPerson>() {
                    override fun areItemsTheSame(oldItem: CommentsWithPerson,
                                                 newItem: CommentsWithPerson): Boolean {
                        return oldItem.commentsUid == newItem.commentsUid
                    }

                    override fun areContentsTheSame(oldItem: CommentsWithPerson,
                                                    newItem: CommentsWithPerson): Boolean {
                        return oldItem.commentsPersonUid == newItem.commentsPersonUid &&
                                oldItem.commentsText == newItem.commentsText &&
                                oldItem.commentsDateTimeUpdated == newItem.commentsDateTimeUpdated
                    }
                }

    }
}