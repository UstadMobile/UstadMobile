package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemPeerAllocationButtonHeaderBinding
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class PeerAllocationHeaderAdapter(val eventHandler: PeerReviewerAllocationEditFragmentEventHandler)
    : SingleItemRecyclerViewAdapter<PeerAllocationHeaderAdapter.PeerReviewerAllocationHolder>(true) {

    class PeerReviewerAllocationHolder(var itemBinding: ItemPeerAllocationButtonHeaderBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeerReviewerAllocationHolder {
        val viewHolder = PeerReviewerAllocationHolder(
            ItemPeerAllocationButtonHeaderBinding.inflate(LayoutInflater.from(parent.context),
                parent, false).also {
                it.listener = eventHandler
            })
        return viewHolder
    }
}