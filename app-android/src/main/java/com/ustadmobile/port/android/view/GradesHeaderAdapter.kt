package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.HeaderGradesBinding
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.OnListFilterOptionSelectedListener
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class GradesHeaderAdapter(val filterOptions: List<ListFilterIdOption> = listOf(),
                          onFilterOptionSelected: OnListFilterOptionSelectedListener? = null,
                          selectedOption: ListFilterIdOption? = filterOptions.firstOrNull())
    : SingleItemRecyclerViewAdapter<GradesHeaderAdapter.GradesHeaderViewHolder>(false) {

    class GradesHeaderViewHolder(var itemBinding: HeaderGradesBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    var viewHolder: GradesHeaderViewHolder? = null

    var listener: OnListFilterOptionSelectedListener? = onFilterOptionSelected
        set(value){
            field = value
            viewHolder?.itemBinding?.onListFilterOptionSelected = value
        }

    var selectedFilterOption: Int = selectedOption?.optionId ?: 0
        set(value){
            field = value
            viewHolder?.itemBinding?.selectedFilterOption = value
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradesHeaderViewHolder {
        viewHolder =  GradesHeaderViewHolder(
            HeaderGradesBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ).also {
                it.filterOptions = filterOptions
                it.onListFilterOptionSelected = listener
                it.selectedFilterOption = selectedFilterOption
            }
        )
        return viewHolder as GradesHeaderViewHolder
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }



}