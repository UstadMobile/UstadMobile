package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemClazzworkDetailDescriptionBinding
import com.ustadmobile.lib.db.entities.ClazzWorkWithSubmission

class ClazzWorkBasicDetailsRecyclerAdapter(clazzWork: ClazzWorkWithSubmission?,
                                           visible: Boolean = false)
    : ListAdapter<ClazzWorkWithSubmission,
        ClazzWorkBasicDetailsRecyclerAdapter.ClazzWorkDetailViewHolder>(ClazzWorkDetailOverviewFragment.DU_CLAZZWORKWITHSUBMISSION) {

    var visible: Boolean = visible
        set(value) {
            if(field == value)
                return

            field = value
        }

    class ClazzWorkDetailViewHolder(var itemBinding: ItemClazzworkDetailDescriptionBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: ClazzWorkDetailViewHolder? = null

    var _clazzWork: ClazzWorkWithSubmission? = clazzWork
        set(value){
            if(field == value)
                return
            notifyDataSetChanged()
            viewHolder?.itemBinding?.clazzWorkWithSubmission = value
            viewHolder?.itemView?.tag = value?.clazzWorkUid?:0L
            notifyDataSetChanged()
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzWorkDetailViewHolder {
        return ClazzWorkDetailViewHolder(
                ItemClazzworkDetailDescriptionBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.clazzWorkWithSubmission = _clazzWork
                    viewHolder?.itemView?.tag = _clazzWork?.clazzWorkUid?:0L
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun getItemCount(): Int {
        return if(visible) 1 else 0
    }

    override fun onBindViewHolder(holder: ClazzWorkDetailViewHolder, position: Int) {
        viewHolder?.itemView?.tag = _clazzWork?.clazzWorkUid?:0L
        viewHolder?.itemBinding?.itemClazzworkDetailDescriptionCl?.tag =
                _clazzWork?.clazzWorkUid?:0L
    }
}