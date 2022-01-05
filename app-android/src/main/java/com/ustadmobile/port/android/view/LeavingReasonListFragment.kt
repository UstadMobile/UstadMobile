package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemLeavingReasonListBinding
import com.ustadmobile.core.controller.LeavingReasonListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.LeavingReasonListView
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


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

        mPresenter = LeavingReasonListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner).withViewLifecycle()

        mDataRecyclerViewAdapter = LeavingReasonListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.add_leaving_reason)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
                createNewText)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ustadFragmentTitle = requireContext().getString(R.string.select_leaving_reason)
        fabManager?.text = requireContext().getText(R.string.leaving_reason)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = false
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout)
            mPresenter?.handleClickCreateNewFab()
        else
            super.onClick(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.leavingReasonDao


    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<LeavingReason> = object
            : DiffUtil.ItemCallback<LeavingReason>() {
            override fun areItemsTheSame(oldItem: LeavingReason,
                                         newItem: LeavingReason): Boolean {
                return oldItem.leavingReasonUid == newItem.leavingReasonUid
            }

            override fun areContentsTheSame(oldItem: LeavingReason,
                                            newItem: LeavingReason): Boolean {
                return oldItem == newItem
            }
        }
    }
}