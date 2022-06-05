
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemScopedGrantListBinding
import com.ustadmobile.core.controller.ScopedGrantListItemListener
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ustadmobile.core.controller.ScopedGrantEditPresenter
import com.ustadmobile.lib.db.entities.ScopedGrantWithName
import com.ustadmobile.port.android.view.ext.setSelectedIfInList


class ScopedGrantListRecyclerAdapter(
    var itemListener: ScopedGrantListItemListener?
): SelectablePagedListAdapter<ScopedGrantWithName, ScopedGrantListRecyclerAdapter.ScopedGrantListViewHolder>(
    DIFF_CALLBACK
) {

    class ScopedGrantListViewHolder(val itemBinding: ItemScopedGrantListBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScopedGrantListViewHolder {
        val itemBinding = ItemScopedGrantListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.itemListener = itemListener
        itemBinding.selectablePagedListAdapter = this
        return ScopedGrantListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ScopedGrantListViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.scopedGrantFlagMessageIds = ScopedGrantEditPresenter.PERMISSION_MESSAGE_ID_LIST
        holder.itemBinding.scopedGrant = item
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ScopedGrantWithName> = object
            : DiffUtil.ItemCallback<ScopedGrantWithName>() {
            override fun areItemsTheSame(
                oldItem: ScopedGrantWithName,
                newItem: ScopedGrantWithName
            ): Boolean {
                return oldItem.sgUid == newItem.sgUid
            }

            override fun areContentsTheSame(
                oldItem: ScopedGrantWithName,
                newItem: ScopedGrantWithName
            ): Boolean {
                return oldItem.sgPermissions == newItem.sgPermissions
                        && oldItem.name == newItem.name
            }
        }
    }

}