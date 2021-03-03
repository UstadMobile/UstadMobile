package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzEnrolmentWithClazzWorkProgressListBinding
import com.ustadmobile.core.controller.ClazzWorkDetailProgressListPresenter
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithClazzWorkProgress
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class ClazzWorkProgressListRecyclerAdapter(
        var presenter: ClazzWorkDetailProgressListPresenter?)
    : SelectablePagedListAdapter<ClazzEnrolmentWithClazzWorkProgress,
        ClazzWorkProgressListRecyclerAdapter.ClazzWorkProgressListViewHolder>(DIFF_CALLBACK) {

    class ClazzWorkProgressListViewHolder(
            val itemBinding: ItemClazzEnrolmentWithClazzWorkProgressListBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : ClazzWorkProgressListViewHolder {
        val itemBinding = ItemClazzEnrolmentWithClazzWorkProgressListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        itemBinding.presenter = presenter
        itemBinding.selectablePagedListAdapter = this
        return ClazzWorkProgressListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ClazzWorkProgressListViewHolder,
                                  position: Int) {
        val item = getItem(position)
        holder.itemBinding.clazzEnrolmentWithClazzWorkProgress = item
        holder.itemView.tag = item?.personUid
        holder.itemBinding.progressBar2.tag = item?.personUid
        holder.itemBinding.itemPersonLine2Text.tag = item?.personUid
        holder.itemBinding.itemClazzworkProgressMemberName.tag = item?.personUid
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        presenter = null
    }

    companion object{
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzEnrolmentWithClazzWorkProgress> = object
            : DiffUtil.ItemCallback<ClazzEnrolmentWithClazzWorkProgress>() {
            override fun areItemsTheSame(oldItem: ClazzEnrolmentWithClazzWorkProgress,
                                         newItem: ClazzEnrolmentWithClazzWorkProgress): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: ClazzEnrolmentWithClazzWorkProgress,
                                            newItem: ClazzEnrolmentWithClazzWorkProgress): Boolean {
                return oldItem == newItem
            }
        }
    }
}
