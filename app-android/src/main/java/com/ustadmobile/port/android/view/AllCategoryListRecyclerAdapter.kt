
package com.ustadmobile.port.android.view


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemAllCategoryListBinding
import com.ustadmobile.core.controller.CategoryListItemListener
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.lib.db.entities.Category
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class AllCategoryListRecyclerAdapter(var itemListener: CategoryListItemListener?, val context: Context)
    : SelectablePagedListAdapter<Category,
        AllCategoryListRecyclerAdapter.ProductListViewHolder>(DIFF_CALLBACK) {

    private var viewHolder:ProductListViewHolder ? = null

    var isAdmin: Boolean? = false
        set(value){
            field = value
            viewHolder?.itemBinding?.categoryDeleteVisible = value

        }

    class ProductListViewHolder(val itemBinding: ItemAllCategoryListBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListViewHolder {
        val itemBinding = ItemAllCategoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.itemListener = itemListener
        itemBinding.selectablePagedListAdapter = this
        itemBinding.categoryDeleteVisible = isAdmin
        itemBinding.locale = UMAndroidUtil.getCurrentLocale(context)

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
        viewHolder = null
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