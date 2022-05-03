package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemMessageNewSendBinding
import com.ustadmobile.core.controller.NewCommentItemListener
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class NewMessageSendRecyclerViewAdapter(
        var itemListener: NewCommentItemListener?,
        hintText: String? = null)
    : SingleItemRecyclerViewAdapter<NewMessageSendRecyclerViewAdapter.NewMessageViewHolder>() {

    var hintText: String? = hintText
        set(value) {
            field = value
            viewHolder?.itemBinding?.hintText = value
            currentViewHolder?.itemBinding?.hintText = value
        }


    fun clearComment(){
        currentViewHolder?.itemBinding?.comment = ""
        currentViewHolder?.itemBinding?.itemCommentNewCommentEt?.text?.clear()
    }

    class NewMessageViewHolder(var itemBinding: ItemMessageNewSendBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: NewMessageViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewMessageViewHolder {
        return NewMessageViewHolder(
            ItemMessageNewSendBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.hintText = hintText
                    it.listener = itemListener
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
        viewHolder = null
    }

}