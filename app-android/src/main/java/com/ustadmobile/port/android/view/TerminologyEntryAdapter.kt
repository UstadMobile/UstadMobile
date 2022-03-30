package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemCourseTerminologyEntryBinding
import com.ustadmobile.lib.db.entities.TerminologyEntry

class TerminologyEntryAdapter() : ListAdapter<TerminologyEntry,
        TerminologyEntryAdapter.TerminologyEntryHolder>(DIFF_CALLBACK) {

    class TerminologyEntryHolder(val itemBinding: ItemCourseTerminologyEntryBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TerminologyEntryHolder {
        val itemBinding = ItemCourseTerminologyEntryBinding.inflate(LayoutInflater.from(parent.context), parent,
            false)
        return TerminologyEntryHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: TerminologyEntryHolder, position: Int) {

    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<TerminologyEntry> = object
            : DiffUtil.ItemCallback<TerminologyEntry>() {
            override fun areItemsTheSame(oldItem: TerminologyEntry,
                                         newItem: TerminologyEntry): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: TerminologyEntry,
                                            newItem: TerminologyEntry): Boolean {
                return oldItem === newItem
            }
        }
    }

}