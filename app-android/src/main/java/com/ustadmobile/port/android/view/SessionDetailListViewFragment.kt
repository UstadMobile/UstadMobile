package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemStatementSessionDetailListBinding
import com.ustadmobile.core.controller.SessionDetailListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SessionDetailListView
import com.ustadmobile.lib.db.entities.StatementWithSessionDetailDisplay
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class SessionDetailListViewFragment(): UstadListViewFragment<StatementWithSessionDetailDisplay, StatementWithSessionDetailDisplay>(),
        SessionDetailListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: SessionDetailListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in StatementWithSessionDetailDisplay>?
        get() = mPresenter

    class StatementWithSessionsDetailListRecyclerAdapter(var presenter: SessionDetailListPresenter?): SelectablePagedListAdapter<StatementWithSessionDetailDisplay, StatementWithSessionsDetailListRecyclerAdapter.StatementWithSessionDetailListViewHolder>(DIFF_CALLBACK) {

        class StatementWithSessionDetailListViewHolder(val itemBinding: ItemStatementSessionDetailListBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatementWithSessionDetailListViewHolder {
            val itemBinding = ItemStatementSessionDetailListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return StatementWithSessionDetailListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: StatementWithSessionDetailListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.statement = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = SessionDetailListPresenter(requireContext(), arguments.toStringMap(),
                this, di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = StatementWithSessionsDetailListRecyclerAdapter(mPresenter)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter()
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = false
    }

    override var personWithContentTitle: String? = null
        get() = field
        set(value) {
            field = value
            ustadFragmentTitle = value
        }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.statementDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<StatementWithSessionDetailDisplay> = object
            : DiffUtil.ItemCallback<StatementWithSessionDetailDisplay>() {
            override fun areItemsTheSame(oldItem: StatementWithSessionDetailDisplay,
                                         newItem: StatementWithSessionDetailDisplay): Boolean {
                return oldItem.statementUid == newItem?.statementUid
            }

            override fun areContentsTheSame(oldItem: StatementWithSessionDetailDisplay,
                                            newItem: StatementWithSessionDetailDisplay): Boolean {
                return oldItem == newItem
            }
        }
    }

}