package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.toughra.ustadmobile.databinding.ItemLeavingReasonListBinding
import com.ustadmobile.core.controller.LeavingReasonListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.LeavingReasonListView
import com.ustadmobile.lib.db.entities.LeavingReason

import com.ustadmobile.core.view.GetResultMode
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.LeavingReasonListItemListener
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter


class LeavingReasonListFragment(): UstadListViewFragment<LeavingReason, LeavingReason>(),
        LeavingReasonListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: LeavingReasonListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in LeavingReason>?
        get() = mPresenter

    class LeavingReasonListRecyclerAdapter(var presenter: LeavingReasonListPresenter?): SelectablePagedListAdapter<LeavingReason, LeavingReasonListRecyclerAdapter.LeavingReasonListViewHolder>(DIFF_CALLBACK) {

        class LeavingReasonListViewHolder(val itemBinding: ItemLeavingReasonListBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeavingReasonListViewHolder {
            val itemBinding = ItemLeavingReasonListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return LeavingReasonListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: LeavingReasonListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.leavingReason = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = LeavingReasonListPresenter(requireContext(), arguments.toStringMap(), this,
                viewLifecycleOwner, di)

        mDataRecyclerViewAdapter = LeavingReasonListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.create_new,
                requireContext().getString(R.string.leavingreason))
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText)
        return view
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.leavingreason)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout)
            navigateToEditEntity(null, R.id.leavingreason_edit_dest, LeavingReason::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = TODO("Provide repo e.g. dbRepo.LeavingReasonDao")


    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<LeavingReason> = object
            : DiffUtil.ItemCallback<LeavingReason>() {
            override fun areItemsTheSame(oldItem: LeavingReason,
                                         newItem: LeavingReason): Boolean {
                oldItem.leavingReasonUid == newItem.leavingReasonUid
            }

            override fun areContentsTheSame(oldItem: LeavingReason,
                                            newItem: LeavingReason): Boolean {
                return oldItem == newItem
            }
        }
    }
}