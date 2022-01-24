package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemBitmaskBinding
import com.ustadmobile.core.model.BitmaskFlag

class BitmaskViewHolder(val itemBinding: ItemBitmaskBinding): RecyclerView.ViewHolder(itemBinding.root)

class BitmaskRecyclerViewAdapter: ListAdapter<BitmaskFlag, BitmaskViewHolder>(DIFFUTIL_BITMASKFLAG) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BitmaskViewHolder {
        return BitmaskViewHolder(
            ItemBitmaskBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: BitmaskViewHolder, position: Int) {
        val bitmaskFlag = getItem(position)
        holder.itemView.tag = bitmaskFlag.flagVal
        holder.itemBinding.bitmaskFlag = bitmaskFlag
    }

    companion object {
        val DIFFUTIL_BITMASKFLAG = object: DiffUtil.ItemCallback<BitmaskFlag>() {
            override fun areItemsTheSame(oldItem: BitmaskFlag, newItem: BitmaskFlag): Boolean {
                return oldItem.flagVal == newItem.flagVal
            }

            //Because we are using two way binding, we must make sure that the binding is always
            // to the same object in memory so any changes are saved back to the expected object
            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: BitmaskFlag, newItem: BitmaskFlag): Boolean {
                return oldItem === newItem
            }
        }
    }
}