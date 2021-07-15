package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemContentEntryDetailAttemptBinding
import com.ustadmobile.core.controller.AttemptListListener
import com.ustadmobile.core.controller.ContentEntryDetailAttemptsListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryDetailAttemptsListView
import com.ustadmobile.lib.db.entities.PersonWithAttemptsSummary
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class ContentEntryDetailAttemptsListFragment(): UstadListViewFragment<PersonWithAttemptsSummary, PersonWithAttemptsSummary>(),
        ContentEntryDetailAttemptsListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: ContentEntryDetailAttemptsListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonWithAttemptsSummary>?
        get() = mPresenter

    class PersonWithStatementDisplayListRecyclerAdapter(var listener: AttemptListListener?): SelectablePagedListAdapter<PersonWithAttemptsSummary, PersonWithStatementDisplayListRecyclerAdapter.PersonWithStatementDisplayListViewHolder>(DIFF_CALLBACK) {

        class PersonWithStatementDisplayListViewHolder(val itemBinding: ItemContentEntryDetailAttemptBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonWithStatementDisplayListViewHolder {
            val itemBinding = ItemContentEntryDetailAttemptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.listener = listener
            itemBinding.selectablePagedListAdapter = this
            return PersonWithStatementDisplayListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: PersonWithStatementDisplayListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.person = item
            holder.itemView.tag = item?.personUid
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            listener = null
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = ContentEntryDetailAttemptsListPresenter(requireContext(), arguments.toStringMap(),
                this, di, viewLifecycleOwner).withViewLifecycle()

        mDataRecyclerViewAdapter = PersonWithStatementDisplayListRecyclerAdapter(mPresenter)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(
                onClickSort = this, sortOrderOption = mPresenter?.sortOptions?.get(0))
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.statementDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithAttemptsSummary> = object
            : DiffUtil.ItemCallback<PersonWithAttemptsSummary>() {
            override fun areItemsTheSame(oldItem: PersonWithAttemptsSummary,
                                         newItem: PersonWithAttemptsSummary): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: PersonWithAttemptsSummary,
                                            newItem: PersonWithAttemptsSummary): Boolean {
                return oldItem == newItem
            }
        }
    }

}