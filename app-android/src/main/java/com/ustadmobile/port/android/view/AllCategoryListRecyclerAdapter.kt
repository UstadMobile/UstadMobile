
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemAllCategoryListBinding
import com.ustadmobile.core.controller.CategoryListItemListener
import com.ustadmobile.lib.db.entities.Category
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class AllCategoryListRecyclerAdapter(var itemListener: CategoryListItemListener?)
    : SelectablePagedListAdapter<Category,
        AllCategoryListRecyclerAdapter.ProductListViewHolder>(DIFF_CALLBACK) {

    class ProductListViewHolder(val itemBinding: ItemAllCategoryListBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListViewHolder {
        val itemBinding = ItemAllCategoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.itemListener = itemListener
        itemBinding.selectablePagedListAdapter = this
        return ProductListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ProductListViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.category = item
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
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