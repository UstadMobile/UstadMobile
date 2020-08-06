package com.ustadmobile.port.android.view.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCreatenewBinding
import java.lang.IllegalArgumentException

/**
 * Simple recyclerviewadapter that can show (or remove) a clickable view. It can be used
 * with MergedRecyclerViewAdapter.
 */
class NewItemRecyclerViewAdapter(onClickNewItem: View.OnClickListener? = null,
                                 createNewText: String? = null,
                                var headerStringId: Int = 0,
                                var headerLayoutId: Int = 0): ListAdapter<Int, RecyclerView.ViewHolder>(DIFFUTIL_NEWITEM) {

    val currentHolderList: List<Int>
        get() = (if(headerLayoutId != 0) listOf(ITEM_HEADERHOLDER) else listOf()) +
                (if(newItemVisible) listOf(ITEM_NEWITEMHOLDER) else listOf())


    var newItemVisible: Boolean = false
        set(value) {
            field = value
            submitList(currentHolderList)
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

    class HeaderItemViewHolder(var view: View): RecyclerView.ViewHolder(view)

    private val boundNewItemViewHolders = mutableListOf<NewItemViewHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            ITEM_NEWITEMHOLDER -> NewItemViewHolder(ItemCreatenewBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false).also {
                it.onClickNew = onClickNewItem
                it.createNewText = createNewText
            })
            ITEM_HEADERHOLDER -> HeaderItemViewHolder(LayoutInflater.from(parent.context)
                    .inflate(headerLayoutId, parent, false))
            else -> throw IllegalArgumentException("Illegal viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is NewItemViewHolder)
            boundNewItemViewHolders += holder
        else if(holder is HeaderItemViewHolder) {
            (holder.view as? TextView)?.text = holder.view.context.getText(headerStringId)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if(holder is NewItemViewHolder)
            boundNewItemViewHolders -= holder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        onClickNewItem = null
        boundNewItemViewHolders.clear()
    }

    companion object {

        const val ITEM_NEWITEMHOLDER = 1

        const val ITEM_HEADERHOLDER = 2

        val DIFFUTIL_NEWITEM = object: DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }
        }
    }


}