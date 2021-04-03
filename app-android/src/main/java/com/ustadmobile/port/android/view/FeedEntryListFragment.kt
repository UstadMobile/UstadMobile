package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ustadmobile.core.controller.FeedEntryListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.FeedEntryListView
import com.ustadmobile.lib.db.entities.FeedEntry
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter


class FeedEntryListFragment(): UstadListViewFragment<FeedEntry, FeedEntry>(),
        FeedEntryListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: FeedEntryListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in FeedEntry>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = FeedEntryListPresenter(requireContext(), arguments.toStringMap(), this,
                 di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = FeedEntryListRecyclerAdapter(mPresenter)
        this.mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
            "")
        return view
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = null

}