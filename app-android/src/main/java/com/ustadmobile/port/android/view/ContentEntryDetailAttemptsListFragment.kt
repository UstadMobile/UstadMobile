package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ItemContentEntryDetailAttemptBinding
import com.ustadmobile.core.controller.ContentEntryDetailAttemptsListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryDetailAttemptsListView
import com.ustadmobile.lib.db.entities.PersonWithStatementDisplay
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class ContentEntryDetailAttemptsListFragment(): UstadListViewFragment<PersonWithStatementDisplay, PersonWithStatementDisplay>(),
        ContentEntryDetailAttemptsListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: ContentEntryDetailAttemptsListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonWithStatementDisplay>?
        get() = mPresenter

    class PersonWithStatementDisplayListRecyclerAdapter(var presenter: ContentEntryDetailAttemptsListPresenter?): SelectablePagedListAdapter<PersonWithStatementDisplay, PersonWithStatementDisplayListRecyclerAdapter.PersonWithStatementDisplayListViewHolder>(DIFF_CALLBACK) {

        class PersonWithStatementDisplayListViewHolder(val itemBinding: ItemContentEntryDetailAttemptBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonWithStatementDisplayListViewHolder {
            val itemBinding = ItemContentEntryDetailAttemptBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return PersonWithStatementDisplayListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: PersonWithStatementDisplayListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.person = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = ContentEntryDetailAttemptsListPresenter(requireContext(), arguments.toStringMap(),
                this, di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = PersonWithStatementDisplayListRecyclerAdapter(mPresenter)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.personDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithStatementDisplay> = object
            : DiffUtil.ItemCallback<PersonWithStatementDisplay>() {
            override fun areItemsTheSame(oldItem: PersonWithStatementDisplay,
                                         newItem: PersonWithStatementDisplay): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: PersonWithStatementDisplay,
                                            newItem: PersonWithStatementDisplay): Boolean {
                return oldItem == newItem
            }
        }
    }

}