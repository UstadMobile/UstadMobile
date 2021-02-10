package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzEnrollmentWithClazzWorkProgressListBinding
import com.ustadmobile.core.controller.ClazzWorkDetailProgressListPresenter
import com.ustadmobile.lib.db.entities.ClazzEnrollmentWithClazzWorkProgress
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class ClazzWorkProgressListRecyclerAdapter(
        var presenter: ClazzWorkDetailProgressListPresenter?)
    : SelectablePagedListAdapter<ClazzEnrollmentWithClazzWorkProgress,
        ClazzWorkProgressListRecyclerAdapter.ClazzWorkProgressListViewHolder>(DIFF_CALLBACK) {

    class ClazzWorkProgressListViewHolder(
            val itemBinding: ItemClazzEnrollmentWithClazzWorkProgressListBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : ClazzWorkProgressListViewHolder {
        val itemBinding = ItemClazzEnrollmentWithClazzWorkProgressListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        itemBinding.presenter = presenter
        itemBinding.selectablePagedListAdapter = this
        return ClazzWorkProgressListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ClazzWorkProgressListViewHolder,
                                  position: Int) {
        val item = getItem(position)
        holder.itemBinding.clazzEnrollmentWithClazzWorkProgress = item
        holder.itemView.tag = item?.mClazzEnrollment?.clazzEnrollmentUid?:0L
        holder.itemBinding.progressBar2.tag = item?.mClazzEnrollment?.clazzEnrollmentUid?:0L
        holder.itemBinding.itemPersonLine2Text.tag = item?.mClazzEnrollment?.clazzEnrollmentUid?:0L
        holder.itemBinding.itemClazzworkProgressMemberName.tag = item?.mClazzEnrollment?.clazzEnrollmentUid?:0L
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        presenter = null
    }

    companion object{
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzEnrollmentWithClazzWorkProgress> = object
            : DiffUtil.ItemCallback<ClazzEnrollmentWithClazzWorkProgress>() {
            override fun areItemsTheSame(oldItem: ClazzEnrollmentWithClazzWorkProgress,
                                         newItem: ClazzEnrollmentWithClazzWorkProgress): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: ClazzEnrollmentWithClazzWorkProgress,
                                            newItem: ClazzEnrollmentWithClazzWorkProgress): Boolean {
                return oldItem.mClazzEnrollment == newItem.mClazzEnrollment
            }
        }
    }
}
