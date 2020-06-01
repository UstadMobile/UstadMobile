package com.ustadmobile.port.android.view.util

import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView

abstract class SingleItemRecyclerViewAdapter<VH: RecyclerView.ViewHolder>(visible: Boolean = false): RecyclerView.Adapter<VH>() {

    var visible: Boolean = visible
        set(value) {
            if(field == value)
                return

            field = value

            if(value) {
                notifyItemChanged(0)
            }else {
                notifyItemRemoved(0)
            }
        }

    var currentViewHolder: VH? = null
        private set

    override fun getItemCount(): Int {
        return if(visible) 1 else 0
    }

    @CallSuper
    override fun onBindViewHolder(holder: VH, position: Int) {
        currentViewHolder = holder
    }

    override fun onViewRecycled(holder: VH) {
        currentViewHolder = null
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        currentViewHolder = null
    }
}