package com.ustadmobile.port.android.view.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCreatenewBinding

/**
 * Simple recyclerviewadapter that can show (or remove) a clickable view. It can be used
 * with MergedRecyclerViewAdapter.
 */
class NewItemRecyclerViewAdapter(onClickNewItem: View.OnClickListener? = null,
                                 createNewText: String? = null): ListAdapter<Any, NewItemRecyclerViewAdapter.NewItemViewHolder>(DIFFUTIL_NEWITEM) {

    var newItemVisible: Boolean = false
        set(value) {
            if(field == value)
                return

            if(value)
                submitList(listOf(Any()))
            else
                submitList(listOf())

            field = value
        }

    var createNewText: String? = createNewText
        set(value) {
            field = value
            boundNewItemViewHolders.forEach {
                it.itemBinding.createNewText = value
            }
        }

    var onClickNewItem: View.OnClickListener? = onClickNewItem
        set(value) {
            field = value
            boundNewItemViewHolders.forEach {
                it.itemBinding.onClickNew = onClickNewItem
            }
        }

    class NewItemViewHolder(var itemBinding: ItemCreatenewBinding): RecyclerView.ViewHolder(itemBinding.root)

    private val boundNewItemViewHolders = mutableListOf<NewItemViewHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewItemViewHolder {
        return NewItemViewHolder(ItemCreatenewBinding.inflate(LayoutInflater.from(parent.context),
                parent, false).also {
            it.onClickNew = onClickNewItem
            it.createNewText = createNewText
        })
    }

    override fun onBindViewHolder(holder: NewItemViewHolder, position: Int) {
        boundNewItemViewHolders += holder
    }

    override fun onViewRecycled(holder: NewItemViewHolder) {
        boundNewItemViewHolders -= holder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        onClickNewItem = null
        boundNewItemViewHolders.clear()
    }

    companion object {
        val DIFFUTIL_NEWITEM = object: DiffUtil.ItemCallback<Any>() {
            override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
                return true
            }

            override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
                return true
            }
        }
    }


}