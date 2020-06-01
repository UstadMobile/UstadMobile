package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCommentNewBinding

class NewCommentRecyclerViewAdapter(onClickNewComment: View.OnClickListener? = null,
                                    hintText: String? = null)
    : RecyclerView.Adapter<NewCommentRecyclerViewAdapter.NewCommentViewHolder>() {




    var hintText: String? = hintText
        set(value) {
            field = value
            boundNewItemViewHolders.forEach {
                it.itemBinding.hintText = value
            }
        }

    var onClickNewItem: View.OnClickListener? = onClickNewComment
        set(value) {
            field = value
            boundNewItemViewHolders.forEach {
                it.itemBinding.onClickNewComment = onClickNewItem
            }
        }

    class NewCommentViewHolder(var itemBinding: ItemCommentNewBinding)
        : RecyclerView.ViewHolder(itemBinding.root)


    private val boundNewItemViewHolders = mutableListOf<NewCommentViewHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewCommentViewHolder {
        return NewCommentViewHolder(
                ItemCommentNewBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.onClickNewComment = onClickNewItem
                    it.hintText = hintText

                })
    }


    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        onClickNewItem = null
        boundNewItemViewHolders.clear()
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onBindViewHolder(holder: NewCommentViewHolder, position: Int) {

    }


}