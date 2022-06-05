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
import com.toughra.ustadmobile.databinding.ItemCourseBottomSheetOptionBinding


data class TitleDescBottomSheetOption(val title: String, val desc: String, val optionCode: Int)

class TitleDescBottomSheetOptionFragment(
    private val optionsList: List<TitleDescBottomSheetOption>,
    private var onOptionSelected: TitleDescBottomSheetOptionSelectedListener?
) : BottomSheetDialogFragment(), TitleDescBottomSheetOptionSelectedListener {

        class BottomSheetOptionViewHolder(val mBinding: ItemCourseBottomSheetOptionBinding) : RecyclerView.ViewHolder(mBinding.root)

        inner class BottomSheetOptionRecyclerViewAdapter(): ListAdapter<TitleDescBottomSheetOption, BottomSheetOptionViewHolder>(DIFF_UTIL_BOTTOM_OPTION) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomSheetOptionViewHolder {
                return BottomSheetOptionViewHolder(ItemCourseBottomSheetOptionBinding.inflate(LayoutInflater.from(parent.context),
                        parent, false).also {
                    it.selectListener = this@TitleDescBottomSheetOptionFragment
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

        override fun onBottomSheetOptionSelected(optionSelected: TitleDescBottomSheetOption) {
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
            val DIFF_UTIL_BOTTOM_OPTION = object: DiffUtil.ItemCallback<TitleDescBottomSheetOption>() {
                override fun areItemsTheSame(oldItem: TitleDescBottomSheetOption, newItem: TitleDescBottomSheetOption) =
                        (oldItem.optionCode == newItem.optionCode)

                override fun areContentsTheSame(oldItem: TitleDescBottomSheetOption, newItem: TitleDescBottomSheetOption) =
                        (oldItem.title == newItem.title && oldItem.desc == newItem.desc)

            }
        }
}
