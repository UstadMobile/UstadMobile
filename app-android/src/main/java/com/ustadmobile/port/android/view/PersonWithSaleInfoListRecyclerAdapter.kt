
package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemPersonWithSaleInfoListBinding
import com.ustadmobile.core.controller.PersonWithSaleInfoListItemListener
import com.ustadmobile.lib.db.entities.PersonWithSaleInfo

import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ustadmobile.port.android.view.ext.setSelectedIfInList


class PersonWithSaleInfoListRecyclerAdapter(var itemListener: PersonWithSaleInfoListItemListener?)
    : SelectablePagedListAdapter<PersonWithSaleInfo,
        PersonWithSaleInfoListRecyclerAdapter.PersonWithSaleInfoListViewHolder>(DIFF_CALLBACK) {

    class PersonWithSaleInfoListViewHolder(val itemBinding: ItemPersonWithSaleInfoListBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonWithSaleInfoListViewHolder {
        val itemBinding = ItemPersonWithSaleInfoListBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
        itemBinding.listener = itemListener
        return PersonWithSaleInfoListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: PersonWithSaleInfoListViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.person = item
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithSaleInfo> = object
            : DiffUtil.ItemCallback<PersonWithSaleInfo>() {
            override fun areItemsTheSame(oldItem: PersonWithSaleInfo,
                                         newItem: PersonWithSaleInfo): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: PersonWithSaleInfo,
                                            newItem: PersonWithSaleInfo): Boolean {
                return oldItem == newItem
            }
        }
    }

}