package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzAssignmentScoreDetailBinding
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class ScoreRecyclerAdapter : SingleItemRecyclerViewAdapter<
        ScoreRecyclerAdapter.ClazzAssignmentScoreDetailViewHolder>() {

    class ClazzAssignmentScoreDetailViewHolder(var itemBinding: ItemClazzAssignmentScoreDetailBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: ClazzAssignmentScoreDetailViewHolder? = null

    var score: ContentEntryStatementScoreProgress? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.score = value
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzAssignmentScoreDetailViewHolder {
        return ClazzAssignmentScoreDetailViewHolder(
                ItemClazzAssignmentScoreDetailBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.score = score
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: ClazzAssignmentScoreDetailViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

    }

}