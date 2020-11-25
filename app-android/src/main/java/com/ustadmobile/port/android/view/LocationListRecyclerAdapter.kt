
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemLocationListBinding
import com.ustadmobile.core.controller.LocationListItemListener
import com.ustadmobile.lib.db.entities.Location

import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ustadmobile.port.android.view.ext.setSelectedIfInList


class LocationListRecyclerAdapter(var itemListener: LocationListItemListener?): SelectablePagedListAdapter<Location, LocationListRecyclerAdapter.LocationListViewHolder>(DIFF_CALLBACK) {

    class LocationListViewHolder(val itemBinding: ItemLocationListBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationListViewHolder {
        val itemBinding = ItemLocationListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.itemListener = itemListener
        itemBinding.selectablePagedListAdapter = this
        return LocationListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: LocationListViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.location = item
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Location> = object
            : DiffUtil.ItemCallback<Location>() {
            override fun areItemsTheSame(oldItem: Location,
                                         newItem: Location): Boolean {
                TODO("e.g. insert primary keys here return oldItem.location == newItem.location")
            }

            override fun areContentsTheSame(oldItem: Location,
                                            newItem: Location): Boolean {
                return oldItem == newItem
            }
        }
    }

}