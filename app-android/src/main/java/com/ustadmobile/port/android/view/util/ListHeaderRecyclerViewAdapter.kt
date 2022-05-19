package com.ustadmobile.port.android.view.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCreatenewBinding
import com.toughra.ustadmobile.databinding.ItemFilterChipsBinding
import com.toughra.ustadmobile.databinding.ItemSortHeaderOptionBinding
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.OnListFilterOptionSelectedListener
import com.ustadmobile.core.util.SortOrderOption

/**
 * This is a RecyclerViewAdapter that provides various utilities that are often found at the top of a
 * list including an option to add a new item, sort order, a header, and choice chips that
 * user can use to filter the list.
 *
 * It can be used with a MergeAdapter
 */
class ListHeaderRecyclerViewAdapter(onClickNewItem: View.OnClickListener? = null,
                                    createNewText: String? = null,
                                    var headerStringId: Int = 0,
                                    headerLayoutId: Int = 0,
                                    onClickSort: View.OnClickListener? = null,
                                    val sortOrderOption: SortOrderOption? = null,
                                    filterOptions: List<ListFilterIdOption> = listOf(),
                                    onFilterOptionSelected: OnListFilterOptionSelectedListener? = null,
                                    selectedFilterOption: ListFilterIdOption? = filterOptions.firstOrNull())

    : ListAdapter<Int, RecyclerView.ViewHolder>(DIFFUTIL_NEWITEM), OnListFilterOptionSelectedListener {

    val currentHolderList: List<Int>
        get() = (if (!filterOptions.isNullOrEmpty()) listOf(ITEM_FILTER_CHIPS) else listOf()) +
                (if (sortOrderOption != null) listOf(ITEM_SORT_HOLDER) else listOf()) +
                (if (headerLayoutId != 0) listOf(ITEM_HEADERHOLDER) else listOf()) +
                (if (newItemVisible) listOf(ITEM_NEWITEMHOLDER) else listOf())


    var newItemVisible: Boolean = false
        set(value) {
            field = value
            submitList(currentHolderList)
        }

    var headerLayoutId: Int = headerLayoutId
        set(value) {
            field = value
            submitList(currentHolderList)
        }

    init{
        takeIf { sortOrderOption != null }.apply {
            submitList(currentHolderList)
        }
        takeIf { filterOptions.isNotEmpty() }.apply {
            submitList(currentHolderList)
        }
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

    var headerStringText: String? = null
        set(value){
            field = value
            boundHeaderViewHolders.forEach{
                (it.view as TextView).text = value ?: it.view.context.getText(headerStringId)
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

    var filterOptions: List<ListFilterIdOption> = filterOptions
        set(value) {
            if(field == value)
                return

            field = value

            boundFilterOptionViewHolders.forEach {
                it.itemBinding.filterOptions = value.map { it as IdOption }
            }

            if(selectedFilterOption == null)
                selectedFilterOption = value.firstOrNull()

            submitList(currentHolderList)
        }

    var onFilterOptionSelected: OnListFilterOptionSelectedListener? = onFilterOptionSelected
        set(value) {
            if(field == value)
                return

            field = value
            boundFilterOptionViewHolders.forEach {
                it.itemBinding.onListFilterOptionSelected = value
            }
        }

    var selectedFilterOption: ListFilterIdOption? = selectedFilterOption
        set(value) {
            field = value
            boundFilterOptionViewHolders.forEach {
                it.itemBinding.selectedFilterOption = value?.optionId ?: 0
            }
        }

    class NewItemViewHolder(var itemBinding: ItemCreatenewBinding) : RecyclerView.ViewHolder(itemBinding.root)

    class HeaderItemViewHolder(var view: View) : RecyclerView.ViewHolder(view)

    class SortItemViewHolder(var itemBinding: ItemSortHeaderOptionBinding) : RecyclerView.ViewHolder(itemBinding.root)

    class FilterChipsItemViewHolder(var itemBinding: ItemFilterChipsBinding): RecyclerView.ViewHolder(itemBinding.root)

    private val boundNewItemViewHolders = mutableListOf<NewItemViewHolder>()

    private val boundSortItemViewHolders = mutableListOf<SortItemViewHolder>()

    private val boundFilterOptionViewHolders = mutableListOf<FilterChipsItemViewHolder>()

    private val boundHeaderViewHolders = mutableListOf<HeaderItemViewHolder>()

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
            ITEM_FILTER_CHIPS -> FilterChipsItemViewHolder(ItemFilterChipsBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
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
            (holder.view as? TextView)?.text = headerStringText ?: holder.view.context.getText(headerStringId)
            boundHeaderViewHolders += holder
        } else if (holder is SortItemViewHolder) {
            boundSortItemViewHolders += holder
        }else if(holder is FilterChipsItemViewHolder) {
            holder.itemBinding.filterOptions = filterOptions
            holder.itemBinding.selectedFilterOption = selectedFilterOption?.optionId ?: 0
            holder.itemBinding.onListFilterOptionSelected = this
            boundFilterOptionViewHolders += holder
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is NewItemViewHolder)
            boundNewItemViewHolders -= holder
        else if (holder is SortItemViewHolder)
            boundSortItemViewHolders -= holder
        else if(holder is FilterChipsItemViewHolder) {
            boundFilterOptionViewHolders -= holder
        }else if(holder is HeaderItemViewHolder){
            boundHeaderViewHolders -= holder
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        onClickNewItem = null
        onFilterOptionSelected = null
        boundNewItemViewHolders.clear()
        boundSortItemViewHolders.clear()
        boundFilterOptionViewHolders.clear()
        boundHeaderViewHolders.clear()
    }

    override fun onListFilterOptionSelected(filterOptionId: ListFilterIdOption) {
        selectedFilterOption = filterOptionId
        onFilterOptionSelected?.onListFilterOptionSelected(filterOptionId)
    }

    companion object {

        const val ITEM_NEWITEMHOLDER = 1

        const val ITEM_HEADERHOLDER = 2

        const val ITEM_SORT_HOLDER = 3

        const val ITEM_FILTER_CHIPS = 4

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