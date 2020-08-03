package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzMemberWithClazzWorkProgressListBinding
import com.ustadmobile.core.controller.ClazzWorkDetailProgressListPresenter
import com.ustadmobile.lib.db.entities.ClazzMemberWithClazzWorkProgress
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class ClazzWorkProgressListRecyclerAdapter(
        var presenter: ClazzWorkDetailProgressListPresenter?)
    : SelectablePagedListAdapter<ClazzMemberWithClazzWorkProgress,
        ClazzWorkProgressListRecyclerAdapter.ClazzWorkProgressListViewHolder>(DIFF_CALLBACK) {

    class ClazzWorkProgressListViewHolder(
            val itemBinding: ItemClazzMemberWithClazzWorkProgressListBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : ClazzWorkProgressListViewHolder {
        val itemBinding = ItemClazzMemberWithClazzWorkProgressListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        itemBinding.presenter = presenter
        itemBinding.selectablePagedListAdapter = this
        return ClazzWorkProgressListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ClazzWorkProgressListViewHolder,
                                  position: Int) {
        val item = getItem(position)
        holder.itemBinding.clazzMemberWithClazzWorkProgress = item
        holder.itemView.tag = item?.mClazzMember?.clazzMemberUid?:0L
        holder.itemBinding.progressBar2.tag = item?.mClazzMember?.clazzMemberUid?:0L
        holder.itemBinding.itemPersonLine2Text.tag = item?.mClazzMember?.clazzMemberUid?:0L
        holder.itemBinding.itemClazzworkProgressMemberName.tag = item?.mClazzMember?.clazzMemberUid?:0L
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        presenter = null
    }

    companion object{
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzMemberWithClazzWorkProgress> = object
            : DiffUtil.ItemCallback<ClazzMemberWithClazzWorkProgress>() {
            override fun areItemsTheSame(oldItem: ClazzMemberWithClazzWorkProgress,
                                         newItem: ClazzMemberWithClazzWorkProgress): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: ClazzMemberWithClazzWorkProgress,
                                            newItem: ClazzMemberWithClazzWorkProgress): Boolean {
                return oldItem.mClazzMember == newItem.mClazzMember
            }
        }
    }
}
