package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.core.controller.FeedEntryListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.FeedEntryListView
import com.ustadmobile.lib.db.entities.FeedEntry
import com.ustadmobile.lib.db.entities.FeedSummary
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter


class FeedEntryListFragment(): UstadListViewFragment<FeedEntry, FeedEntry>(),
        FeedEntryListView, View.OnClickListener{

    private var mPresenter: FeedEntryListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in FeedEntry>?
        get() = mPresenter

    override var summaryStats: FeedSummary? = null
        set(value) {
            field = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        autoMergeRecyclerViewAdapter = false
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = FeedEntryListPresenter(requireContext(), arguments.toStringMap(), this,
                 di, viewLifecycleOwner)

        mDataRecyclerViewAdapter = FeedEntryListRecyclerAdapter(mPresenter)
        this.mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
            "")
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMergeRecyclerViewAdapter = ConcatAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = null

    companion object {
        val DIFF_UTIL_FEED_SUMMARY : DiffUtil.ItemCallback<FeedSummary> = object: DiffUtil.ItemCallback<FeedSummary>() {
            override fun areItemsTheSame(oldItem: FeedSummary, newItem: FeedSummary): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: FeedSummary, newItem: FeedSummary): Boolean {
                return oldItem == newItem
            }
        }
    }

}