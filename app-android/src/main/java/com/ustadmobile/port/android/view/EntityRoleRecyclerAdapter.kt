package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemEntityRoleListBinding
import com.ustadmobile.core.controller.RoleEditPresenter
import com.ustadmobile.lib.db.entities.EntityRoleWithNameAndRole

interface EntityRoleItemHandler {

    fun handleClickEntityRole(entityRole: EntityRoleWithNameAndRole)
    fun handleRemoveEntityRole(entityRole: EntityRoleWithNameAndRole)
}

class EntityRoleRecyclerAdapter(private val editMode: Boolean = false,
        var handler: EntityRoleItemHandler) : ListAdapter<EntityRoleWithNameAndRole,
        EntityRoleRecyclerAdapter.EntityRoleViewHolder>(DIFF_ENTITYROLE) {

    class EntityRoleViewHolder(val binding: ItemEntityRoleListBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : EntityRoleViewHolder {

        return EntityRoleViewHolder(ItemEntityRoleListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false).also {
            it.editMode = editMode
            it.bitMaskFlags = RoleEditPresenter.FLAGS_AVAILABLE
            it.handler = handler
        })
    }

    override fun onBindViewHolder(holder: EntityRoleViewHolder, position: Int) {
        holder.binding.entityRole = getItem(position)
    }

    companion object{
        val DIFF_ENTITYROLE =
                object: DiffUtil.ItemCallback<EntityRoleWithNameAndRole>() {
            override fun areItemsTheSame(oldItem: EntityRoleWithNameAndRole,
                                         newItem: EntityRoleWithNameAndRole): Boolean {
                return oldItem.erUid == newItem.erUid
            }

            override fun areContentsTheSame(oldItem: EntityRoleWithNameAndRole,
                                            newItem: EntityRoleWithNameAndRole): Boolean {
                return oldItem == newItem
            }
        }

    }
}