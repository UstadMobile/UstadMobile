package com.ustadmobile.port.android.view

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.view.children
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentListBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.OnSortOptionSelected
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ListViewAddMode
import com.ustadmobile.core.view.SelectionOption
import com.ustadmobile.core.view.UstadListView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.port.android.view.ext.repoLoadingStatus
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

abstract class UstadListViewFragment<RT, DT> : UstadBaseFragment(),
        UstadListView<RT, DT>, Observer<PagedList<DT>>, MessageIdSpinner.OnMessageIdOptionSelectedListener,
        OnSortOptionSelected, View.OnClickListener {

    protected var mRecyclerView: RecyclerView? = null

    internal var mUstadListHeaderRecyclerViewAdapter: ListHeaderRecyclerViewAdapter? = null

    protected var mListStatusAdapter: ListStatusRecyclerViewAdapter<DT>? = null

    internal var mDataRecyclerViewAdapter: SelectablePagedListAdapter<DT, *>? = null

    protected var mMergeRecyclerViewAdapter: ConcatAdapter? = null

    internal var mDataBinding: FragmentListBinding? = null

    protected var currentLiveData: LiveData<PagedList<DT>>? = null

    protected var dbRepo: UmAppDatabase? = null

    private val systemImpl: UstadMobileSystemImpl by instance()

    /**
     * Whether or not UstadListViewFragment should attempt to manage the MergeAdapter. The MergeAdapter
     * is normally used to add a create new item at the start of the list when the addmode is FIRST_ITEM
     * and the user has permission to do so.
     *
     * This can be set to false to stop this e.g. where there are multiple different types of item
     * in a list.
     */
    protected open var autoMergeRecyclerViewAdapter = true

    protected open var autoShowFabOnAddPermission = true

    protected var mActivityWithFab: UstadListViewActivityWithFab? = null
        get() {
            /*
             The getter will return null so that if the current fragment is not actually visible
             no changes will be sent through
             */
            return if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                field
            } else {
                null
            }
        }
        set(value) {
            field = value
        }

    override var selectionOptions: List<SelectionOption>? = null
        get() = field
        set(value) {
            field = value
            actionMode?.invalidate()
        }

    // See https://developer.android.com/guide/topics/ui/menus#CAB

    /**
     * This iscor a Contextual Action Mode Callback that handles showing list selection mode
     */
    private class ListViewActionModeCallback<RT, DT>(var fragmentHost: UstadListViewFragment<RT, DT>?) : ActionMode.Callback {

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val selectedItemsList = fragmentHost?.mDataRecyclerViewAdapter?.selectedItemsLiveData?.value
                    ?: return false
            val option = SelectionOption.values().first { it.commandId == item.itemId }
            fragmentHost?.listPresenter?.handleClickSelectionOption(selectedItemsList, option)
            mode.finish()
            return true
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.clear()
            val fragmentContext = fragmentHost?.requireContext() ?: return false
            val systemImpl: UstadMobileSystemImpl =
                (fragmentHost as? UstadListViewFragment<*, *>)?.systemImpl ?: return false

            fragmentHost?.selectionOptions?.forEachIndexed { index, item ->
                val optionText = systemImpl.getString(item.messageId, fragmentContext)
                menu.add(0, item.commandId, index, optionText).apply {
                    setIcon(SELECTION_ICONS_MAP[item] ?: R.drawable.ic_delete_black_24dp)
                }
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            fragmentHost?.actionMode = null
            fragmentHost?.mDataRecyclerViewAdapter?.clearSelection()
            fragmentHost?.mRecyclerView?.children?.forEach {
                it.isSelected = false

                //Check if this is the first item where the data view is actually nested
                if (it is ViewGroup && it.id == R.id.item_createnew_linearlayout1) {
                    it.children.forEach { it.isSelected = false }
                }
            }

            //This MUST be done to avoid a memory leak. Finishing the action mode does not seem to
            //clear the reference to this callback.
            fragmentHost = null
        }
    }

    private var actionModeCallback: ActionMode.Callback? = null

    private var numItemsSelected = 0

    protected val selectionObserver = object : Observer<List<DT>> {
        override fun onChanged(t: List<DT>?) {
            val actionModeVal = actionMode

            if (!t.isNullOrEmpty() && actionModeVal == null) {
                val actionModeCallbackVal =
                        ListViewActionModeCallback(this@UstadListViewFragment).also {
                            this@UstadListViewFragment.actionModeCallback = it
                        }
                actionMode = (activity as? AppCompatActivity)?.startSupportActionMode(
                        actionModeCallbackVal)
                listPresenter?.handleSelectionOptionChanged(t)
            } else if (actionModeVal != null && t.isNullOrEmpty()) {
                actionModeVal.finish()
            }

            val listSize = t?.size ?: 0
            if (listSize > 0) {
                actionMode?.title = requireContext().getString(R.string.items_selected, listSize)
            }
        }
    }

    protected var actionMode: ActionMode? = null

    /**
     *
     */
    protected abstract val displayTypeRepo: Any?

    protected abstract val listPresenter: UstadListPresenter<*, in DT>?

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mDataBinding = FragmentListBinding.inflate(inflater, container, false).also {
            rootView = it.root
            mRecyclerView = it.fragmentListRecyclerview
            it.fragmentListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        }

        val accountManager: UstadAccountManager by instance()
        dbRepo = on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_REPO)

        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        searchManager?.searchListener = listPresenter
        menu.findItem(R.id.menu_search).isVisible = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDataBinding?.presenter = listPresenter
        mListStatusAdapter = ListStatusRecyclerViewAdapter(viewLifecycleOwner)

        if (autoMergeRecyclerViewAdapter) {
            mMergeRecyclerViewAdapter = ConcatAdapter(mUstadListHeaderRecyclerViewAdapter,
                    mDataRecyclerViewAdapter, mListStatusAdapter)
            mRecyclerView?.adapter = mMergeRecyclerViewAdapter
            mDataRecyclerViewAdapter?.selectedItemsLiveData?.observe(this.viewLifecycleOwner,
                    selectionObserver)
        }

        fabManager?.onClickListener = {
            mDataBinding?.presenter?.handleClickCreateNewFab()
        }
        fabManager?.takeIf { autoShowFabOnAddPermission }?.visible =
                (addMode == ListViewAddMode.FAB)
        fabManager?.icon = R.drawable.ic_add_white_24dp




        listPresenter?.onCreate(savedInstanceState.toStringMap())
    }

    override fun onDestroyView() {
        mRecyclerView?.adapter = null
        mDataRecyclerViewAdapter = null
        mRecyclerView = null
        mDataBinding = null
        mRecyclerView = null
        currentLiveData?.removeObserver(this)
        currentLiveData = null
        actionModeCallback = null
        actionMode?.finish()
        dbRepo = null

        super.onDestroyView()
    }

    override var addMode: ListViewAddMode = ListViewAddMode.NONE
        get() = field
        set(value) {
            mDataBinding?.addMode = value
            mUstadListHeaderRecyclerViewAdapter.takeIf { autoMergeRecyclerViewAdapter }?.newItemVisible =
                    (value == ListViewAddMode.FIRST_ITEM)
            fabManager?.takeIf { autoShowFabOnAddPermission }?.visible =
                    (value == ListViewAddMode.FAB)

            field = value
        }

    override var listFilterOptionChips: List<ListFilterIdOption>? = null
        set(value) {
            mUstadListHeaderRecyclerViewAdapter?.filterOptions = value ?: listOf()
            field = value
        }

    override var checkedFilterOptionChip: ListFilterIdOption?
        get() = mUstadListHeaderRecyclerViewAdapter?.selectedFilterOption
        set(value) {
            mUstadListHeaderRecyclerViewAdapter?.selectedFilterOption = value
        }

    override var list: DataSource.Factory<Int, DT>? = null
        set(value) {
            currentLiveData?.removeObserver(this)
            val displayTypeRepoVal = displayTypeRepo ?: return
            currentLiveData = value?.asRepositoryLiveData(displayTypeRepoVal)
            mListStatusAdapter?.repositoryLoadStatus = currentLiveData?.repoLoadingStatus
            mListStatusAdapter?.pagedListLiveData = currentLiveData
            currentLiveData?.observe(viewLifecycleOwner, this)
            field = value
        }

    override fun onChanged(t: PagedList<DT>?) {
        mDataRecyclerViewAdapter?.submitList(t)
    }

    override fun onMessageIdOptionSelected(view: AdapterView<*>?,
                                           messageIdOption: IdOption) {
        listPresenter?.handleClickSortOrder(messageIdOption)
    }

    override fun onClickSort(sortOption: SortOrderOption) {
        mUstadListHeaderRecyclerViewAdapter?.sortOptionSelected = sortOption
        listPresenter?.onClickSort(sortOption)
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {
        //do nothing
    }

    override var sortOptions: List<MessageIdOption>? = null
        get() = field
        set(value) {
            field = value
        }


    fun showSortOptionsFrag() {
        SortBottomSheetFragment(listPresenter?.sortOptions, mUstadListHeaderRecyclerViewAdapter?.sortOptionSelected, this).also {
            it.show(childFragmentManager, it.tag)
        }
    }

    override fun onClick(v: View?) {
        if(v?.id == R.id.item_sort_selected_layout)
            showSortOptionsFrag()
        else if(v?.id == R.id.item_createnew_layout) {
            //This is the "Add new " item in picker mode.
            listPresenter?.handleClickAddNewItem()
        }
    }

    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        (activity as? MainActivity)?.showSnackBar(message, action, actionMessageId)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivityWithFab = context as? UstadListViewActivityWithFab
    }

    override fun onDetach() {
        super.onDetach()
        mActivityWithFab = null
    }

    companion object {

        val SELECTION_ICONS_MAP =
                mapOf(SelectionOption.EDIT to R.drawable.ic_edit_white_24dp,
                        SelectionOption.DELETE to R.drawable.ic_delete_black_24dp,
                        SelectionOption.MOVE to R.drawable.ic_move,
                        SelectionOption.HIDE to R.drawable.ic_baseline_visibility_off_24,
                        SelectionOption.UNHIDE to R.drawable.ic_baseline_visibility_24)

    }

}