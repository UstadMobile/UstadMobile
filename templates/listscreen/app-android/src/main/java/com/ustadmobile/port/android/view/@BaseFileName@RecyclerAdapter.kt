
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.Item@ListItemName@ListBinding
import com.ustadmobile.core.controller.@BaseFileName@ItemListener
import com.ustadmobile.lib.db.entities.@Entity@
@DisplayEntity_Import@
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ustadmobile.port.android.view.ext.setSelectedIfInList


class @BaseFileName@RecyclerAdapter(
    var itemListener: @BaseFileName@ItemListener?
): SelectablePagedListAdapter<@DisplayEntity@, @BaseFileName@RecyclerAdapter.@Entity@ListViewHolder>(
    DIFF_CALLBACK
) {

    class @Entity@ListViewHolder(val itemBinding: Item@ListItemName@ListBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): @Entity@ListViewHolder {
        val itemBinding = Item@ListItemName@ListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.itemListener = itemListener
        itemBinding.selectablePagedListAdapter = this
        return @Entity@ListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: @Entity@ListViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.@Entity_VariableName@ = item
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<@DisplayEntity@> = object
            : DiffUtil.ItemCallback<@DisplayEntity@>() {
            override fun areItemsTheSame(
                oldItem: @DisplayEntity@,
                newItem: @DisplayEntity@
            ): Boolean {
                TODO("e.g. insert primary keys here return oldItem.@Entity_VariableName@ == newItem.@Entity_VariableName@")
            }

            override fun areContentsTheSame(
                oldItem: @DisplayEntity@,
                newItem: @DisplayEntity@
            ): Boolean {
                //Check only those fields that are displayed to the user to minimize refreshes
                TODO("e.g. return oldItem.field1 == newItem.field1 && oldItem.field2 == newItem.field2")
            }
        }
    }

}