package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzworkDetailDescriptionBinding
import com.ustadmobile.lib.db.entities.ClazzWorkWithSubmission
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class ClazzWorkBasicDetailsRecyclerAdapter()
    : SingleItemRecyclerViewAdapter<ClazzWorkBasicDetailsRecyclerAdapter.ClazzWorkDetailViewHolder>() {

    class ClazzWorkDetailViewHolder(var itemBinding: ItemClazzworkDetailDescriptionBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: ClazzWorkDetailViewHolder? = null

    var clazzWork: ClazzWorkWithSubmission? = null
        set(value){
            if(field == value)
                return
            field = value
            viewHolder?.itemBinding?.clazzWorkWithSubmission = value
            viewHolder?.itemView?.tag = value?.clazzWorkUid?:0L

        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzWorkDetailViewHolder {
        return ClazzWorkDetailViewHolder(
                ItemClazzworkDetailDescriptionBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.clazzWorkWithSubmission = clazzWork
                    viewHolder?.itemView?.tag = clazzWork?.clazzWorkUid?:0L
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: ClazzWorkDetailViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        viewHolder?.itemView?.tag = clazzWork?.clazzWorkUid?:0L
        viewHolder?.itemBinding?.itemClazzworkDetailDescriptionCl?.tag =
                clazzWork?.clazzWorkUid?:0L
    }
}