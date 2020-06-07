package com.ustadmobile.port.android.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemSimpleButtonBinding
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter

class SimpleButtonRecyclerAdapter(heading: String, val buttonHandler: SimpleButtonHandler)
    : SingleItemRecyclerViewAdapter<SimpleButtonRecyclerAdapter.SimpleHeadingViewHolder>() {

    var buttonText: String? = heading
        set(value) {
            field = value
            viewHolder?.itemBinding?.buttonText = value
        }

    class SimpleHeadingViewHolder(var itemBinding: ItemSimpleButtonBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    private var viewHolder: SimpleHeadingViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleHeadingViewHolder {
        return SimpleHeadingViewHolder(
                ItemSimpleButtonBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.buttonText = buttonText
                    it.mHandler = buttonHandler
                })
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        viewHolder = null
    }


}