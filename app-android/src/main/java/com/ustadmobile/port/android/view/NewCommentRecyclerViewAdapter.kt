package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCommentNewBinding
import com.ustadmobile.core.controller.CommentListener
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class NewCommentRecyclerViewAdapter(
        var itemListener: CommentListener?,
        hintText: String? = null, commentPublic: Boolean)
    : SingleItemRecyclerViewAdapter<NewCommentRecyclerViewAdapter.NewCommentViewHolder>() {

    var hintText: String? = hintText
        set(value) {
            field = value
            viewHolder?.itemBinding?.hintText = value
        }

    private var newCommentHandler: CommentListener? = itemListener
        set(value) {
            field = value
            viewHolder?.itemBinding?.commentHandler = newCommentHandler
        }

    private var publicMode: Boolean = commentPublic
        set(value){
            field = value
        }

    class NewCommentViewHolder(var itemBinding: ItemCommentNewBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: NewCommentViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewCommentViewHolder {
        return NewCommentViewHolder(
                ItemCommentNewBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.commentHandler = newCommentHandler
                    it.hintText = hintText
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
        viewHolder = null
    }

}