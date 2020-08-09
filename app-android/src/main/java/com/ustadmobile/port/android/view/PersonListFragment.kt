package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemPersonListItemBinding
import com.ustadmobile.core.controller.OnSearchSubmitted
import com.ustadmobile.core.controller.PersonListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.NewItemRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class PersonListFragment() : UstadListViewFragment<Person, PersonWithDisplayDetails>(),
        PersonListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener {

    private var mPresenter: PersonListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonWithDisplayDetails>?
        get() = mPresenter

    class PersonListViewHolder(val itemBinding: ItemPersonListItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

    class PersonListRecyclerAdapter(var presenter: PersonListPresenter?)
        : SelectablePagedListAdapter<PersonWithDisplayDetails, PersonListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonListViewHolder {
            val itemBinding = ItemPersonListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return PersonListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: PersonListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.person = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = PersonListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = PersonListRecyclerAdapter(mPresenter)
        val createNewText = requireContext().getString(R.string.create_new,
                requireContext().getString(R.string.person))
        mNewItemRecyclerViewAdapter = NewItemRecyclerViewAdapter(this, createNewText,
                onClickSort = this, sortOrderOption = mPresenter?.sortOptions?.get(0))
        return view
    }

    override fun onResume() {
        //Set text first so that it will expand to the correct size as required
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.person)
        super.onResume()
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if (view?.id == R.id.item_createnew_layout)
            navigateToEditEntity(null, R.id.person_edit_dest, Person::class.java)
        else {
            super.onClick(view)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.personDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<PersonWithDisplayDetails> = object
            : DiffUtil.ItemCallback<PersonWithDisplayDetails>() {
            override fun areItemsTheSame(oldItem: PersonWithDisplayDetails,
                                         newItem: PersonWithDisplayDetails): Boolean {
                return oldItem.personUid == newItem.personUid
            }

            override fun areContentsTheSame(oldItem: PersonWithDisplayDetails,
                                            newItem: PersonWithDisplayDetails): Boolean {
                return oldItem == newItem
            }
        }
    }
}