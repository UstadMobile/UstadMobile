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
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemBottomSheetOptionBinding

data class BottomSheetOption(val iconId: Int, val label: String, val optionCode: Int)

class OptionsBottomSheetFragment(private val optionsList: List<BottomSheetOption>,
                                 private var onOptionSelected: BottomSheetOptionSelectedListener?): BottomSheetDialogFragment(), BottomSheetOptionSelectedListener {

    class BottomSheetOptionViewHolder(val mBinding: ItemBottomSheetOptionBinding) : RecyclerView.ViewHolder(mBinding.root)

    inner class BottomSheetOptionRecyclerViewAdapter(): ListAdapter<BottomSheetOption, BottomSheetOptionViewHolder>(DIFF_UTIL_BOTTOM_OPTION) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomSheetOptionViewHolder {
            return BottomSheetOptionViewHolder(ItemBottomSheetOptionBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false).also {
                it.selectListener = this@OptionsBottomSheetFragment
            })
        }

        override fun onBindViewHolder(holder: BottomSheetOptionViewHolder, position: Int) {
            holder.mBinding.option = getItem(position)
        }
    }

    private var mRecyclerAdapter: BottomSheetOptionRecyclerViewAdapter? = null

    private var mRecyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRecyclerView = inflater.inflate(R.layout.fragment_options_bottom_sheet, container, false) as RecyclerView

        mRecyclerAdapter = BottomSheetOptionRecyclerViewAdapter().apply {
            submitList(optionsList)
        }

        mRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        mRecyclerView?.adapter = mRecyclerAdapter

        return mRecyclerView
    }

    override fun onBottomSheetOptionSelected(optionSelected: BottomSheetOption) {
        takeIf { this.dialog?.isShowing == true }?.dismiss()
        onOptionSelected?.onBottomSheetOptionSelected(optionSelected)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mRecyclerView?.adapter = null
        mRecyclerView = null
        onOptionSelected = null
    }

    companion object {
        val DIFF_UTIL_BOTTOM_OPTION = object: DiffUtil.ItemCallback<BottomSheetOption>() {
            override fun areItemsTheSame(oldItem: BottomSheetOption, newItem: BottomSheetOption) =
                    (oldItem.optionCode == newItem.optionCode)

            override fun areContentsTheSame(oldItem: BottomSheetOption, newItem: BottomSheetOption) =
                    (oldItem.iconId == newItem.iconId && oldItem.label == newItem.label)

        }
    }

}