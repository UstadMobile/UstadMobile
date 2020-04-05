package com.ustadmobile.port.android.view.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemCreatenewBinding
import com.toughra.ustadmobile.databinding.ItemCreatenewContainerBinding

/**
 * The PagedListAdapterWithNewItem will create a special viewholder for the first item that wraps
 * the original viewholder in a layout with options to create a new item. This function will
 * return the nested ViewHolder if that is the case, or the original ViewHolder
 */
fun RecyclerView.ViewHolder.getDataItemViewHolder(): RecyclerView.ViewHolder {
    return (this as? PagedListAdapterWithNewItem.NewItemViewHolder)?.nestedViewHolder ?: this
}

/**
 * This adapter helps make a list where the user can select an existing item, or choose to create
 * a new one. The first ViewHolder is always a NewItemViewHolder, which contains a nested view holder
 * for the actual data item itself.
 *
 * NewItemViewHolder uses a linear layout to show (or hide) the option to create a new item.
 */
abstract class PagedListAdapterWithNewItem<T>(
        diffcallback: DiffUtil.ItemCallback<T>,
        newItemVisible: Boolean = false,
        var onClickNewItem: View.OnClickListener? = null,
        var createNewText: String? = null)
    : PagedListAdapter<T, RecyclerView.ViewHolder>(diffcallback) {

    var newItemVisible: Boolean = newItemVisible
        set(value) {
            if(field == value)
                return

            field = value
            boundNewItemViewHolders.forEach { it.createNewItemVisible = value }
        }


    class NewItemViewHolder(itemView: View, val nestedViewHolder: RecyclerView.ViewHolder)
        : RecyclerView.ViewHolder(itemView) {

        var createNewItemVisible: Boolean = false
            set(value) {
                itemView.findViewById<View>(R.id.item_createnew_layout).visibility = if(value) {
                    View.VISIBLE
                }else {
                    View.GONE
                }

                field = value
            }
    }

    val boundNewItemViewHolders = mutableListOf<NewItemViewHolder>()

    override fun getItemViewType(position: Int): Int {
        return if(position == 0) {
            ITEMVIEWTYPE_NEW
        }else {
            ITEMVIEWTYPE_DEFAULT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == ITEMVIEWTYPE_NEW) {
            val newItemBinding = ItemCreatenewContainerBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false)

            newItemBinding.createNewText = createNewText
            newItemBinding.onClickNew = onClickNewItem

            val newItemView = newItemBinding.root

            val viewHolderLinearLayout: LinearLayout = newItemView.findViewById(R.id.item_createnew_linearlayout1)
            val nestedViewHolder = onCreateViewHolder(viewHolderLinearLayout, ITEMVIEWTYPE_DEFAULT)
            viewHolderLinearLayout.addView(nestedViewHolder.itemView)
            return NewItemViewHolder(newItemView, nestedViewHolder).also {
                boundNewItemViewHolders += it
                it.createNewItemVisible = newItemVisible
            }
        }

        throw IllegalStateException("PagedListAdapterWithNewItem can only create new item. " +
                "A viewholder for the item must be created by the child class")
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if(holder is NewItemViewHolder) {
            boundNewItemViewHolders -= holder
        }
    }

    companion object {

        const val ITEMVIEWTYPE_NEW = 2

        const val ITEMVIEWTYPE_DEFAULT = 0

    }

}