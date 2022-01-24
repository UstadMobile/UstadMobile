package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemPersonSessionsListBinding
import com.ustadmobile.core.controller.SessionListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SessionListView
import com.ustadmobile.lib.db.entities.PersonWithSessionsDisplay
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


class SessionListFragment(): UstadListViewFragment<PersonWithSessionsDisplay, PersonWithSessionsDisplay>(),
        SessionListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: SessionListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonWithSessionsDisplay>?
        get() = mPresenter

    class PersonWithSessionsDisplayListRecyclerAdapter(var presenter: SessionListPresenter?): SelectablePagedListAdapter<PersonWithSessionsDisplay, PersonWithSessionsDisplayListRecyclerAdapter.PersonWithSessionDisplayListViewHolder>(DIFF_CALLBACK) {

        class PersonWithSessionDisplayListViewHolder(val itemBinding: ItemPersonSessionsListBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonWithSessionDisplayListViewHolder {
            val itemBinding = ItemPersonSessionsListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return PersonWithSessionDisplayListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: PersonWithSessionDisplayListViewHolder, position: Int) {
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
        mPresenter = SessionListPresenter(requireContext(), arguments.toStringMap(),
                this, di, viewLifecycleOwner).withViewLifecycle()

        mDataRecyclerViewAdapter = PersonWithSessionsDisplayListRecyclerAdapter(mPresenter)
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
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithSessionsDisplay> = object
            : DiffUtil.ItemCallback<PersonWithSessionsDisplay>() {
            override fun areItemsTheSame(oldItem: PersonWithSessionsDisplay,
                                         newItem: PersonWithSessionsDisplay): Boolean {
                return oldItem.contextRegistration == newItem.contextRegistration
            }

            override fun areContentsTheSame(oldItem: PersonWithSessionsDisplay,
                                            newItem: PersonWithSessionsDisplay): Boolean {
                return oldItem == newItem
            }
        }
    }

}