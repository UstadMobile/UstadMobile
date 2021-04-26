
package com.ustadmobile.port.android.view


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzAssignmentDetailStudentProgressBinding
import com.ustadmobile.core.controller.ClazzAssignmentDetailStudentProgressItemListener
import com.ustadmobile.lib.db.entities.ContentEntryWithAttemptsSummary
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class ClazzAssignmentDetailStudentProgressRecyclerAdapter(var itemListener: ClazzAssignmentDetailStudentProgressItemListener?): SelectablePagedListAdapter<ContentEntryWithAttemptsSummary, ClazzAssignmentDetailStudentProgressRecyclerAdapter.ClazzAssignmentListViewHolder>(DIFF_CALLBACK) {

    class ClazzAssignmentListViewHolder(val itemBinding: ItemClazzAssignmentDetailStudentProgressBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzAssignmentListViewHolder {
        val itemBinding = ItemClazzAssignmentDetailStudentProgressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        itemBinding.itemListener = itemListener
        itemBinding.selectablePagedListAdapter = this
        return ClazzAssignmentListViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ClazzAssignmentListViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.entry = item
        holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemListener = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ContentEntryWithAttemptsSummary> = object
            : DiffUtil.ItemCallback<ContentEntryWithAttemptsSummary>() {
            override fun areItemsTheSame(oldItem: ContentEntryWithAttemptsSummary,
                                         newItem: ContentEntryWithAttemptsSummary): Boolean {
                return oldItem.contentEntryUid == newItem.contentEntryUid
            }

            override fun areContentsTheSame(oldItem: ContentEntryWithAttemptsSummary,
                                            newItem: ContentEntryWithAttemptsSummary): Boolean {
                return oldItem == newItem
            }
        }
    }

}