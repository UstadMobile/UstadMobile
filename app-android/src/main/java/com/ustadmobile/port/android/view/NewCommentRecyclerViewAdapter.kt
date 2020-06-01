package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCommentNewBinding

class NewCommentRecyclerViewAdapter(newCommentHandler: NewCommentHandler? = null,
                                    hintText: String? = null, commentPublic: Boolean)
    : RecyclerView.Adapter<NewCommentRecyclerViewAdapter.NewCommentViewHolder>() {

    var hintText: String? = hintText
        set(value) {
            field = value
            viewHolder?.itemBinding?.hintText = value
        }

    var newCommentHandler: NewCommentHandler? = newCommentHandler
        set(value) {
            field = value
            viewHolder?.itemBinding?.mActivity = newCommentHandler
        }

    private var publicMode: Boolean = commentPublic
        set(value){
            field = value
            viewHolder?.itemBinding?.publicComment = value
        }

    class NewCommentViewHolder(var itemBinding: ItemCommentNewBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: NewCommentViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewCommentViewHolder {
        return NewCommentViewHolder(
                ItemCommentNewBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.mActivity = newCommentHandler
                    it.hintText = hintText
                    it.publicComment = publicMode
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        newCommentHandler = null
        viewHolder = null
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onBindViewHolder(holder: NewCommentViewHolder, position: Int) {}
}