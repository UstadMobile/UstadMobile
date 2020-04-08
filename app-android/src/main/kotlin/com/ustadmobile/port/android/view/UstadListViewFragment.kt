package com.ustadmobile.port.android.view

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentListBinding
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ListViewAddMode
import com.ustadmobile.core.view.SelectionOption
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.children
import com.ustadmobile.core.view.UstadListView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.port.android.view.util.PagedListAdapterWithNewItem

interface ListViewResultListener<RT> {

    fun onListItemsSelected(items: List<RT>)

}

abstract class UstadListViewFragment<RT, DT>: UstadBaseFragment(),
        UstadListView<RT, DT>, Observer<PagedList<DT>>, MessageIdSpinner.OnMessageIdOptionSelectedListener {

    protected var mRecyclerView: RecyclerView? = null

    protected var mRecyclerViewAdapter: PagedListAdapterWithNewItem<DT>? = null
        set(value) {
            field = value
            mRecyclerView?.adapter = value
        }

    protected var mDataBinding: FragmentListBinding? = null

    protected var currentLiveData: LiveData<PagedList<DT>>? = null

    protected var mListViewResultListener: ListViewResultListener<RT>? = null

    protected var mActivityWithFab: UstadListViewActivityWithFab? = null
        get() {
            /*
             The getter will return null so that if the current fragment is not actually visible
             no changes will be sent through
             */
            return if(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                field
            }else {
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
            //todo: invalidate options if required
        }

    // See https://developer.android.com/guide/topics/ui/menus#CAB
    private val actionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val selectedItemsList = mRecyclerViewAdapter?.selectedItemsLiveData?.value ?: return false
            val option = SelectionOption.values().first { it.commandId == item.itemId }
            listPresenter?.handleClickSelectionOption(selectedItemsList, option)
            mode.finish()
            return true
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val systemImpl = UstadMobileSystemImpl.instance
            selectionOptions?.forEachIndexed { index, item ->
                menu.add(0, item.commandId, index,
                        systemImpl.getString(item.messageId, requireContext())).apply {
                    val drawable = requireContext().getDrawable(SELECTION_ICONS_MAP[item] ?: R.drawable.ic_delete_black_24dp) ?: return@forEachIndexed
                    DrawableCompat.setTint(drawable, ContextCompat.getColor(requireContext(), R.color.primary_text))
                    icon = drawable
                }
            }

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false // if nothing has changed
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            mRecyclerViewAdapter?.clearSelection()
            mRecyclerView?.children?.forEach {
                it.isSelected = false

                //Check if this is the first item where the data view is actually nested
                if(it is ViewGroup && it.id == R.id.item_createnew_linearlayout1) {
                    it.children.forEach { it.isSelected = false }
                }
            }
        }
    }

    private var numItemsSelected = 0

    protected val selectionObserver = object: Observer<List<DT>> {
        override fun onChanged(t: List<DT>?) {
            val actionModeVal = actionMode
            if(!t.isNullOrEmpty() && actionModeVal == null) {
                actionMode = (activity as? AppCompatActivity)?.startSupportActionMode(actionModeCallback)
            }else if(actionModeVal != null && t.isNullOrEmpty()) {
                actionModeVal.finish()
            }

            val listSize = t?.size ?: 0
            if(listSize > 0) {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mDataBinding = FragmentListBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }
        mRecyclerView = rootView.findViewById(R.id.fragment_list_recyclerview)
        mRecyclerView?.layoutManager = LinearLayoutManager(context)

        return rootView
    }

    override var addMode: ListViewAddMode = ListViewAddMode.NONE
        get() = field
        set(value) {
            mDataBinding?.addMode = value
            mRecyclerViewAdapter?.newItemVisible = (value == ListViewAddMode.FIRST_ITEM)
            mActivityWithFab?.activityFloatingActionButton?.visibility = if(value == ListViewAddMode.FAB) View.VISIBLE else View.GONE
            field = value
        }

    override var list: DataSource.Factory<Int, DT>? = null
        get() = field
        set(value) {
            currentLiveData?.removeObserver(this)
            val displayTypeRepoVal = displayTypeRepo ?: return
            currentLiveData = value?.asRepositoryLiveData(displayTypeRepoVal)
            currentLiveData?.observe(this, this)
        }

    override fun onChanged(t: PagedList<DT>?) {
        mRecyclerViewAdapter?.submitList(t)
    }

    override var sortOptions: List<MessageIdOption>? = null
        get() = field
        set(value) {
            mDataBinding?.sortOptions = value
            field = value
        }

    override fun finishWithResult(result: List<RT>) {
        mListViewResultListener?.onListItemsSelected(result)
    }

    override val viewContext: Any
        get() = requireContext()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListViewResultListener = context as? ListViewResultListener<RT>
        mActivityWithFab = context as? UstadListViewActivityWithFab
    }

    override fun onDetach() {
        super.onDetach()
        mListViewResultListener = null
        mActivityWithFab = null
    }

    override fun onResume() {
        super.onResume()

        val theFab = mActivityWithFab?.activityFloatingActionButton

        theFab?.setOnClickListener {
            mDataBinding?.presenter?.handleClickCreateNewFab()
        }

        theFab?.visibility = View.VISIBLE
        theFab?.visibility = if(addMode == ListViewAddMode.FAB) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    companion object {

        val SELECTION_ICONS_MAP = mapOf(SelectionOption.EDIT to R.drawable.ic_edit_white_24dp,
            SelectionOption.DELETE to R.drawable.ic_delete_black_24dp)

    }

}