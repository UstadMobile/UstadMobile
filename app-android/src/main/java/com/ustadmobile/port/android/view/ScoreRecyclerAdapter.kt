package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzAssignmentDetailBinding
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class ScoreRecyclerAdapter : SingleItemRecyclerViewAdapter<
        ClazzAssignmentBasicDetailRecyclerAdapter.ClazzAssignmentDetailViewHolder>() {

    class ClazzAssignmentDetailViewHolder(var itemBinding: ItemClazzAssignmentDetailBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: ClazzAssignmentDetailViewHolder? = null

    var clazzAssignment: ClazzAssignmentWithMetrics? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.clazzAssignment = value
            viewHolder?.itemView?.tag = value?.caUid?:0L
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzAssignmentBasicDetailRecyclerAdapter.ClazzAssignmentDetailViewHolder {
        return ClazzAssignmentBasicDetailRecyclerAdapter.ClazzAssignmentDetailViewHolder(
                ItemClazzAssignmentDetailBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.clazzAssignment = clazzAssignment
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: ClazzAssignmentBasicDetailRecyclerAdapter.ClazzAssignmentDetailViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

    }

}