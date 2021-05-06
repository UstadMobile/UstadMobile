package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzworkProgressDetailBinding
import com.ustadmobile.lib.db.entities.ClazzWorkWithMetrics


class ClazzWorkMetricsRecyclerAdapter(clazzWork: ClazzWorkWithMetrics?,
                                      visible: Boolean = false)
    : ListAdapter<ClazzWorkWithMetrics,
        ClazzWorkMetricsRecyclerAdapter.ClazzWorkProgressViewHolder>(
        ClazzWorkDetailProgressListFragment.DU_CLAZZWORKWITHMETRICS) {

    var visible: Boolean = visible
        set(value) {
            if(field == value)
                return
            field = value
        }

    class ClazzWorkProgressViewHolder(var itemBinding: ItemClazzworkProgressDetailBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: ClazzWorkProgressViewHolder? = null
    private var clazzWorkVal : ClazzWorkWithMetrics? = clazzWork

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzWorkProgressViewHolder {
        return ClazzWorkProgressViewHolder(
                ItemClazzworkProgressDetailBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun getItemCount(): Int {
        return if(visible) 1 else 0
    }

    override fun onBindViewHolder(holder: ClazzWorkProgressViewHolder, position: Int) {

        holder.itemView.tag = clazzWorkVal?.clazzWorkUid?:0L
        if(currentList.size > 0){
            holder.itemBinding.clazzWorkWithMetrics = getItem(0)
            holder.itemView.tag = getItem(position).clazzWorkUid
        }else {
            holder.itemBinding.clazzWorkWithMetrics = clazzWorkVal
        }
    }
}