package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemMarkFileSubmissionBinding
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class MarkFileSubmissionAdapter: SingleItemRecyclerViewAdapter<
        MarkFileSubmissionAdapter.MarkFileSubmissionViewHolder>() {

    class MarkFileSubmissionViewHolder(var itemBinding: ItemMarkFileSubmissionBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: MarkFileSubmissionViewHolder? = null

    var assignment: ClazzAssignment? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.assignment = value
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkFileSubmissionViewHolder {
        viewHolder = MarkFileSubmissionViewHolder(
                ItemMarkFileSubmissionBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {

                })
        return viewHolder as MarkFileSubmissionViewHolder
    }


    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }


}