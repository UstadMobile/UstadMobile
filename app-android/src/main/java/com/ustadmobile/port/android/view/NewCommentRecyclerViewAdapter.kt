package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCommentNewBinding
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class NewCommentRecyclerViewAdapter(
        var itemListener: OpenSheetListener?,
        hintText: String? = null, commentPublic: Boolean)
    : SingleItemRecyclerViewAdapter<NewCommentRecyclerViewAdapter.NewCommentViewHolder>() {

    var hintText: String? = hintText
        set(value) {
            field = value
            viewHolder?.itemBinding?.hintText = value
        }

    private var openSheetHandler: OpenSheetListener? = itemListener
        set(value) {
            field = value
            viewHolder?.itemBinding?.openSheet = openSheetHandler
        }

    private var publicMode: Boolean = commentPublic
        set(value){
            field = value
            viewHolder?.itemBinding?.publicComment =  value
        }

    class NewCommentViewHolder(var itemBinding: ItemCommentNewBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: NewCommentViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewCommentViewHolder {
        return NewCommentViewHolder(
                ItemCommentNewBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.openSheet = openSheetHandler
                    it.hintText = hintText
                    it.publicComment = publicMode
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
        viewHolder = null
    }

}