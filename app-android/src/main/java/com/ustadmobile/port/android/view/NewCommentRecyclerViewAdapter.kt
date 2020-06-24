package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCommentNewBinding
import com.ustadmobile.core.controller.NewCommentItemListener
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class NewCommentRecyclerViewAdapter(
        var itemListener: NewCommentItemListener?,
        hintText: String? = null, commentPublic: Boolean, entityType: Int, eUid: Long,
        toComment:Long = 0, fromComment: Long = 0)
    : SingleItemRecyclerViewAdapter<NewCommentRecyclerViewAdapter.NewCommentViewHolder>() {

    var hintText: String? = hintText
        set(value) {
            field = value
            viewHolder?.itemBinding?.hintText = value
        }

    var newCommentHandler: NewCommentItemListener? = itemListener
        set(value) {
            field = value
            viewHolder?.itemBinding?.mActivity = newCommentHandler
        }

    private var publicMode: Boolean = commentPublic
        set(value){
            field = value
            viewHolder?.itemBinding?.publicComment = value
        }

    var entityTable : Int = entityType
        set(value){
            field = value
            viewHolder?.itemBinding?.entityType = value
        }

    var entityUid : Long = eUid
        set(value){
            field = value
            viewHolder?.itemBinding?.entityUid = value
        }

    var commentTo : Long = toComment
        get() = field
        set(value){
            field = value
            viewHolder?.itemBinding?.toComment = value
        }

    var commentFrom : Long = fromComment
        get() = field
        set(value){
            field = value
            viewHolder?.itemBinding?.fromComment = value
        }

    class NewCommentViewHolder(var itemBinding: ItemCommentNewBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: NewCommentViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewCommentViewHolder {
        return NewCommentViewHolder(
                ItemCommentNewBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.mActivity = itemListener
                    it.hintText = hintText
                    it.publicComment = publicMode
                    it.entityType = entityTable
                    it.entityUid = entityUid
                    it.toComment = commentTo
                    it.fromComment = commentFrom
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
        viewHolder = null
    }

    override fun getItemCount(): Int {
        return 1
    }

}