
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemPersongroupListItemBinding
import com.ustadmobile.core.controller.PersonGroupListItemListener
import com.ustadmobile.lib.db.entities.PersonGroupWithMemberCount
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class PersonGroupListRecyclerAdapter(var itemListener: PersonGroupListItemListener?)
    : SelectablePagedListAdapter<PersonGroupWithMemberCount,
        PersonGroupListRecyclerAdapter.ProductListViewHolder>(DIFF_CALLBACK) {

    class ProductListViewHolder(val itemBinding: ItemPersongroupListItemBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListViewHolder {
        val itemBinding = ItemPersongroupListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.listener = itemListener
        itemBinding.selectablePagedListAdapter = this
        return ProductListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ProductListViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.personGroup = item
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonGroupWithMemberCount> = object
            : DiffUtil.ItemCallback<PersonGroupWithMemberCount>() {
            override fun areItemsTheSame(oldItem: PersonGroupWithMemberCount,
                                         newItem: PersonGroupWithMemberCount): Boolean {
                return oldItem.groupUid == newItem.groupUid

            }

            override fun areContentsTheSame(oldItem: PersonGroupWithMemberCount,
                                            newItem: PersonGroupWithMemberCount): Boolean {
                return oldItem == newItem
            }
        }
    }

}