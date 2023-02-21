package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemPeerAllocationSubmitterBinding
import com.ustadmobile.core.util.UidOption
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation
import org.kodein.di.DI

class PeerAllocationSubmitterAdapter(submitterList: List<AssignmentSubmitterSummary>, context: Context, di: DI) : ListAdapter<PeerReviewerAllocation,
        PeerAllocationSubmitterAdapter.PeerAllocationSubmitterAdapterHolder>(DIFF_CALLBACK) {

    private var submitterIdOption: MutableList<UidOption> = mutableListOf()

    init {
        submitterList.forEach {
            submitterIdOption.add(UidOption(it.name ?: "", it.submitterUid))
        }
    }


    private var viewHolder: PeerAllocationSubmitterAdapterHolder? = null

    class PeerAllocationSubmitterAdapterHolder(val itemBinding: ItemPeerAllocationSubmitterBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerAllocationSubmitterAdapterHolder {
        viewHolder = PeerAllocationSubmitterAdapterHolder(ItemPeerAllocationSubmitterBinding.inflate(LayoutInflater.from(parent.context), parent,
            false).also {
                it.submitterList = submitterIdOption
        })
        return viewHolder as PeerAllocationSubmitterAdapterHolder
    }

    override fun onBindViewHolder(holder: PeerAllocationSubmitterAdapterHolder, position: Int) {
        holder.itemBinding.reviewer = getItem(position)
        holder.itemBinding.index = position + 1
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PeerReviewerAllocation> = object
            : DiffUtil.ItemCallback<PeerReviewerAllocation>() {
            override fun areItemsTheSame(oldItem: PeerReviewerAllocation,
                                         newItem: PeerReviewerAllocation): Boolean {
                return oldItem.praUid == newItem.praUid
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: PeerReviewerAllocation,
                                            newItem: PeerReviewerAllocation): Boolean {
                return oldItem === newItem
            }
        }
    }

}