package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemSimpleHeadingBinding
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class SimpleHeadingRecyclerAdapter(heading: String)
    : SingleItemRecyclerViewAdapter<SimpleHeadingRecyclerAdapter.SimpleHeadingViewHolder>() {

    var headingText: String? = heading
        set(value) {
            field = value
            viewHolder?.itemBinding?.headingText = value
        }

    class SimpleHeadingViewHolder(var itemBinding: ItemSimpleHeadingBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: SimpleHeadingViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleHeadingViewHolder {
        return SimpleHeadingViewHolder(
                ItemSimpleHeadingBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.headingText = headingText
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }

    override fun onBindViewHolder(holder: SimpleHeadingViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.tag = headingText
    }
}