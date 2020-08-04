package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.toughra.ustadmobile.databinding.FragmentSortOptionListBinding
import com.toughra.ustadmobile.databinding.ItemSortOptionBinding
import com.ustadmobile.core.controller.OnSortOptionSelected
import com.ustadmobile.core.util.SortOrderOption

class SortBottomSheetFragment(val sortOptions: List<SortOrderOption>?, var onSortOptionSelected: OnSortOptionSelected?) : BottomSheetDialogFragment() {

    private var mRecyclerViewAdapter: SortListRecyclerViewAdapter? = null
    private var mBinding: FragmentSortOptionListBinding? = null

    private var mRecyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSortOptionListBinding.inflate(inflater, container, false).also {
            rootView = it.root
            mRecyclerView = it.fragmentSortOrderList
        }

        mRecyclerViewAdapter = SortListRecyclerViewAdapter(onSortOptionSelected)
        mRecyclerView?.adapter = mRecyclerViewAdapter
        mRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        mRecyclerViewAdapter?.submitList(sortOptions)
        return rootView
    }

    class SortListHolder(val itemBinding: ItemSortOptionBinding) : RecyclerView.ViewHolder(itemBinding.root)

    class SortListRecyclerViewAdapter(var onSortOptionSelected: OnSortOptionSelected?) : ListAdapter<SortOrderOption, SortListHolder>(DIFFUTIL_SORT) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortListHolder {
            return SortListHolder(ItemSortOptionBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false))
        }

        override fun onBindViewHolder(holder: SortListHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.sortOption = item
            holder.itemView.tag = item.flag
            holder.itemBinding.sortListener = onSortOptionSelected
        }

    }

    companion object {
        val DIFFUTIL_SORT = object : DiffUtil.ItemCallback<SortOrderOption>() {
            override fun areItemsTheSame(oldItem: SortOrderOption, newItem: SortOrderOption): Boolean {
                return oldItem.flag == newItem.flag
            }

            override fun areContentsTheSame(oldItem: SortOrderOption, newItem: SortOrderOption): Boolean {
                return oldItem == newItem
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        onSortOptionSelected = null
        mBinding = null
        mRecyclerView = null
        mRecyclerViewAdapter = null
    }
}