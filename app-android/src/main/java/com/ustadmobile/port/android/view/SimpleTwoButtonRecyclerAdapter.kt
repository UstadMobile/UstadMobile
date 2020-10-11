package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemSimpleTwoButtonBinding
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class SimpleTwoButtonRecyclerAdapter(primary: String, secondary: String,
                                     val buttonHandler: SimpleTwoButtonHandler)
    : SingleItemRecyclerViewAdapter<SimpleTwoButtonRecyclerAdapter.SimpleHeadingViewHolder>() {

    var primaryText: String? = primary
        set(value) {
            field = value
            viewHolder?.itemBinding?.primaryText = value
        }


    var secondaryText: String? = secondary
        set(value) {
            field = value
            viewHolder?.itemBinding?.secondaryText = value
        }


    class SimpleHeadingViewHolder(var itemBinding: ItemSimpleTwoButtonBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: SimpleHeadingViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleHeadingViewHolder {
        return SimpleHeadingViewHolder(
                ItemSimpleTwoButtonBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.primaryText = primaryText
                    it.secondaryText = secondaryText
                    it.mHandler = buttonHandler
                })
    }

    override fun onBindViewHolder(holder: SimpleHeadingViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.itemView.tag = primaryText
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }


}