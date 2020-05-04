package com.ustadmobile.port.android.view.util

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

abstract class ListAdapterWithNewItem<T, VH: RecyclerView.ViewHolder>(diffCallback: DiffUtil.ItemCallback<T>,
    val blankItem: T, newItemEnabled: Boolean)
    : ListAdapter<T, VH>(diffCallback) {

    var newItemEnabled: Boolean = newItemEnabled
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

    override fun getItemCount(): Int {
        return super.getItemCount() + (if(newItemEnabled) 1 else 0)
    }

    override fun getItemViewType(position: Int): Int {
        return if(newItemEnabled && position == 0) VIEW_TYPE_NEWITEM else super.getItemViewType(position)
    }

    companion object {

        const val VIEW_TYPE_NEWITEM = 2

    }

}