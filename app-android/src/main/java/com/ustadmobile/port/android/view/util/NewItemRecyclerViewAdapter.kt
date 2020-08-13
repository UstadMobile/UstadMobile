package com.ustadmobile.port.android.view.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCreatenewBinding
import com.toughra.ustadmobile.databinding.ItemSortHeaderOptionBinding
import com.ustadmobile.core.util.SortOrderOption
import java.lang.IllegalArgumentException

/**
 * Simple recyclerviewadapter that can show (or remove) a clickable view. It can be used
 * with MergedRecyclerViewAdapter.
 */
class NewItemRecyclerViewAdapter(onClickNewItem: View.OnClickListener? = null,
                                 createNewText: String? = null,
                                 var headerStringId: Int = 0,
                                 var headerLayoutId: Int = 0,
                                 onClickSort: View.OnClickListener? = null,
                                 val sortOrderOption: SortOrderOption? = null) : ListAdapter<Int, RecyclerView.ViewHolder>(DIFFUTIL_NEWITEM) {

    val currentHolderList: List<Int>
        get() = (if (headerLayoutId != 0) listOf(ITEM_HEADERHOLDER) else listOf()) +
                (if (newItemVisible) listOf(ITEM_NEWITEMHOLDER) else listOf()) +
                (if (sortOrderOption != null) listOf(ITEM_SORT_HOLDER) else listOf())


    var newItemVisible: Boolean = false
        set(value) {
            field = value
            submitList(currentHolderList)
        }

    var onClickSort: View.OnClickListener? = onClickSort
        set(value) {
            field = value
            boundSortItemViewHolders.forEach {
                it.itemBinding.onClickSort = onClickSort
            }
        }

    var createNewText: String? = createNewText
        set(value) {
            field = value
            boundNewItemViewHolders.forEach {
                it.itemBinding.createNewText = value
            }
        }

    var sortOptionSelected: SortOrderOption? = sortOrderOption
        set(value) {
            field = value
            boundSortItemViewHolders.forEach {
                it.itemBinding.sortOption = value
            }
        }

    var onClickNewItem: View.OnClickListener? = onClickNewItem
        set(value) {
            field = value
            boundNewItemViewHolders.forEach {
                it.itemBinding.onClickNew = onClickNewItem
            }
        }

    class NewItemViewHolder(var itemBinding: ItemCreatenewBinding) : RecyclerView.ViewHolder(itemBinding.root)

    class HeaderItemViewHolder(var view: View) : RecyclerView.ViewHolder(view)

    class SortItemViewHolder(var itemBinding: ItemSortHeaderOptionBinding) : RecyclerView.ViewHolder(itemBinding.root)

    private val boundNewItemViewHolders = mutableListOf<NewItemViewHolder>()

    private val boundSortItemViewHolders = mutableListOf<SortItemViewHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_NEWITEMHOLDER -> NewItemViewHolder(ItemCreatenewBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false).also {
                it.onClickNew = onClickNewItem
                it.createNewText = createNewText
            })
            ITEM_HEADERHOLDER -> HeaderItemViewHolder(LayoutInflater.from(parent.context)
                    .inflate(headerLayoutId, parent, false))
            ITEM_SORT_HOLDER -> SortItemViewHolder(ItemSortHeaderOptionBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false).also {
                it.onClickSort = onClickSort
                it.sortOption = sortOptionSelected
            })
            else -> throw IllegalArgumentException("Illegal viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NewItemViewHolder)
            boundNewItemViewHolders += holder
        else if (holder is HeaderItemViewHolder) {
            (holder.view as? TextView)?.text = holder.view.context.getText(headerStringId)
        } else if (holder is SortItemViewHolder) {
            boundSortItemViewHolders += holder
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is NewItemViewHolder)
            boundNewItemViewHolders -= holder
        else if (holder is SortItemViewHolder)
            boundSortItemViewHolders -= holder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        onClickNewItem = null
        boundNewItemViewHolders.clear()
        boundSortItemViewHolders.clear()
    }

    companion object {

        const val ITEM_NEWITEMHOLDER = 1

        const val ITEM_HEADERHOLDER = 2

        const val ITEM_SORT_HOLDER = 3

        val DIFFUTIL_NEWITEM = object : DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }
        }
    }


}