package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemSimpleHeadingBinding
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class SimpleHeadingRecyclerAdapter(heading: String)
    : SingleItemRecyclerViewAdapter<SimpleHeadingRecyclerAdapter.SimpleHeadingViewHolder>() {

    var headingText: String? = heading
        set(value) {
            field = value
            viewHolder?.itemBinding?.headingText = value
            viewHolder?.itemView?.tag = headingText
        }

    class SimpleHeadingViewHolder(var itemBinding: ItemSimpleHeadingBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: SimpleHeadingViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleHeadingViewHolder {
        val holder =  SimpleHeadingViewHolder(
                ItemSimpleHeadingBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.headingText = headingText
                })
        holder.itemView.tag = headingText
        viewHolder = holder
        return holder
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