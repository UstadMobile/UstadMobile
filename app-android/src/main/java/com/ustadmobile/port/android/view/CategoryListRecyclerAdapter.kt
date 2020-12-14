
package com.ustadmobile.port.android.view


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCategoryListBinding
import com.ustadmobile.core.controller.CategoryListListener
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.lib.db.entities.Category
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class CategoryListRecyclerAdapter(var itemListener: CategoryListListener?, val context: Context)
    : ListAdapter<Category,
        CategoryListRecyclerAdapter.InventoryTransactionDetailHolder>(DIFF_CALLBACK) {

    class InventoryTransactionDetailHolder(val itemBinding: ItemCategoryListBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryTransactionDetailHolder {
        val itemBinding = ItemCategoryListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        itemBinding.itemListener = itemListener
        itemBinding.locale = UMAndroidUtil.getCurrentLocale(context)
        return InventoryTransactionDetailHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: InventoryTransactionDetailHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.category = item
        //holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Category> = object
            : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(oldItem: Category,
                                         newItem: Category): Boolean {
                return oldItem.categoryUid == newItem.categoryUid

            }

            override fun areContentsTheSame(oldItem: Category,
                                            newItem: Category): Boolean {
                return oldItem == newItem
            }
        }
    }

}