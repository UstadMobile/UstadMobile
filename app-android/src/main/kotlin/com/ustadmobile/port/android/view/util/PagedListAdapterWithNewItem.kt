package com.ustadmobile.port.android.view.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.view.OnClickNewListItemListener

abstract class PagedListAdapterWithNewItem<T>(
        diffcallback: DiffUtil.ItemCallback<T>,
        newItemVisible: Boolean = false,
        var onClickNewItem: OnClickNewListItemListener? = null)
    : PagedListAdapter<T, RecyclerView.ViewHolder>(diffcallback) {

    var newItemVisible: Boolean = newItemVisible
        set(value) {
            if(field == value)
                return

            field = value
            if(value) {
                notifyItemInserted(0)
            }else {
                notifyItemRemoved(0)
            }
        }


    val offset: Int
        get() = (if(newItemVisible) 1 else 0)


    class NewItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun getItemViewType(position: Int): Int {
        return if(position == 0 && newItemVisible) {
            ITEMVIEWTYPE_NEW
        }else {
            ITEMVIEWTYPE_DEFAULT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == ITEMVIEWTYPE_NEW) {
            val newItemView = LayoutInflater
                    .from(parent.context).inflate(R.layout.item_createnew, parent, false)
            newItemView.setOnClickListener { onClickNewItem?.onClickNewListItem() }
            return NewItemViewHolder(newItemView)
        }

        throw IllegalStateException("PagedListAdapterWithNewItem can only create new item. " +
                "A viewholder for the item must be created by the child class")
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + offset
    }

    override fun getItem(position: Int): T? {
        return super.getItem(position - offset)
    }

    companion object {

        const val ITEMVIEWTYPE_NEW = 2

        const val ITEMVIEWTYPE_DEFAULT = 0

    }

}