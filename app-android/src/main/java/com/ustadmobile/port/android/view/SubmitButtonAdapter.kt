package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemAssignmentSubmitButtonBinding
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class SubmitButtonAdapter(val eventHandler: ClazzAssignmentDetailOverviewFragmentEventHandler): SingleItemRecyclerViewAdapter<
        SubmitButtonAdapter.SubmitButtonViewHolder>(false) {

    class SubmitButtonViewHolder(var itemBinding: ItemAssignmentSubmitButtonBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: SubmitButtonViewHolder? = null

    var deadlinePassed: Boolean = false
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.deadlinePassed = value
        }

    var hasFilesToSubmit: Boolean = false
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.hasFilesToSubmit = value
        }

    var unassignedError: String? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.unassignedError = value
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmitButtonViewHolder {
        viewHolder = SubmitButtonViewHolder(
                ItemAssignmentSubmitButtonBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.eventHandler = eventHandler
                    it.deadlinePassed = deadlinePassed
                    it.hasFilesToSubmit = hasFilesToSubmit
                    it.unassignedError = unassignedError
                })
        return viewHolder as SubmitButtonViewHolder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }


}