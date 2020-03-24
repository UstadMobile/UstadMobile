package com.ustadmobile.port.android.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentListBinding
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ListViewAddMode
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

    /**
     *
     */
    protected abstract val displayTypeRepo: Any?

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
    }

    override fun onDetach() {
        super.onDetach()
        mListViewResultListener = null
    }

}