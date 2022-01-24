package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemScopedGrantEditBinding
import com.ustadmobile.core.model.BitmaskMessageId
import com.ustadmobile.core.util.OneToManyJoinEditListener
import com.ustadmobile.lib.db.entities.ScopedGrantAndName


class ScopedGrantAndNameEditRecyclerViewAdapter(var listener: OneToManyJoinEditListener<ScopedGrantAndName>?,
    val permissionList: List<BitmaskMessageId>)
    : ListAdapter<ScopedGrantAndName, ScopedGrantAndNameEditRecyclerViewAdapter.ScopedGrantViewHolder>(DIFFUTIL_SCOPEDGRANTANDNAME){

    class ScopedGrantViewHolder(val binding: ItemScopedGrantEditBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScopedGrantViewHolder {
        return ScopedGrantViewHolder(
            ItemScopedGrantEditBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ScopedGrantViewHolder, position: Int) {
        holder.binding.scopedGrantListener = listener
        holder.binding.scopedGrantFlagMessageIds = permissionList
        holder.binding.scopedGrant = getItem(position)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        listener = null
    }

    companion object {

        val DIFFUTIL_SCOPEDGRANTANDNAME = object : DiffUtil.ItemCallback<ScopedGrantAndName> (){
            override fun areItemsTheSame(
                oldItem: ScopedGrantAndName,
                newItem: ScopedGrantAndName
            ): Boolean {
                return newItem.scopedGrant != null &&
                        newItem.scopedGrant?.sgUid == oldItem.scopedGrant?.sgUid
            }

            override fun areContentsTheSame(
                oldItem: ScopedGrantAndName,
                newItem: ScopedGrantAndName
            ): Boolean {
                return oldItem.scopedGrant != null &&
                        oldItem.scopedGrant?.sgPermissions == newItem.scopedGrant?.sgPermissions &&
                        oldItem.scopedGrant?.sgGroupUid == newItem.scopedGrant?.sgGroupUid &&
                        oldItem.name == newItem.name
            }
        }
    }

}