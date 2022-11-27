package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemPersonListItemBinding
import com.ustadmobile.core.controller.PersonListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ListViewAddMode
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithDisplayDetails
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

interface InviteWithLinkHandler{
    fun handleClickInviteWithLink()
}

class PersonListFragment() : UstadListViewFragment<Person, PersonWithDisplayDetails>(),
        PersonListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener,
        InviteWithLinkHandler{


    override fun handleClickInviteWithLink() {
        mPresenter?.handleClickInviteWithLink()

    }

    private var mPresenter: PersonListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in PersonWithDisplayDetails>?
        get() = mPresenter

    private var inviteWithLinkRecyclerViewAdapter: InviteWithLinkRecyclerViewAdapter? = null

    override var autoMergeRecyclerViewAdapter: Boolean = false

    override var inviteViaLinkVisibile: Boolean
        get() = mUstadListHeaderRecyclerViewAdapter?.newItemVisible ?: false
        set(value) {
            inviteWithLinkRecyclerViewAdapter?.visible = value
        }

    override var addMode: ListViewAddMode = ListViewAddMode.NONE
        get() = field
        set(value) {
            mDataBinding?.addMode = value
            mUstadListHeaderRecyclerViewAdapter?.newItemVisible = (value == ListViewAddMode.FIRST_ITEM)
            fabManager?.visible = (value == ListViewAddMode.FAB)

            field = value
        }

    class PersonListViewHolder(val itemBinding: ItemPersonListItemBinding)
        : RecyclerView.ViewHolder(itemBinding.root)


    class PersonListRecyclerAdapter(var presenter: PersonListPresenter?)
        : SelectablePagedListAdapter<PersonWithDisplayDetails, PersonListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonListViewHolder {
            val itemBinding = ItemPersonListItemBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = PersonListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner).withViewLifecycle()
        
        inviteWithLinkRecyclerViewAdapter = InviteWithLinkRecyclerViewAdapter(this, mPresenter)
        inviteWithLinkRecyclerViewAdapter?.code = arguments?.get(UstadView.ARG_CODE)?.toString()
        inviteWithLinkRecyclerViewAdapter?.entityName = arguments?.get(UstadView.ARG_ENTITY_NAME)?.toString()
        inviteWithLinkRecyclerViewAdapter?.tableId = arguments?.get(UstadView.ARG_CODE_TABLE)?.toString()?.toInt()?:0

        mDataRecyclerViewAdapter = PersonListRecyclerAdapter(mPresenter)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
                requireContext().getString(R.string.add_a_new_person),
                onClickSort = this, sortOrderOption = mPresenter?.sortOptions?.get(0))

        mListStatusAdapter = ListStatusRecyclerViewAdapter(viewLifecycleOwner)
        mMergeRecyclerViewAdapter = ConcatAdapter(mUstadListHeaderRecyclerViewAdapter,
                inviteWithLinkRecyclerViewAdapter,
                mDataRecyclerViewAdapter , mListStatusAdapter)
        mDataBinding?.fragmentListRecyclerview?.adapter = mMergeRecyclerViewAdapter
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
    override fun onClick(v: View?) {
        if (v?.id == R.id.item_createnew_layout)
            mPresenter?.handleClickAddNewItem()
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
                return oldItem.firstNames == newItem.firstNames &&
                        oldItem.lastName == newItem.lastName
            }
        }
    }
}