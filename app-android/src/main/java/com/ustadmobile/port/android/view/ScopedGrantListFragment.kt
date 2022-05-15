package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.ustadmobile.core.controller.ScopedGrantListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ScopedGrantListView
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.lib.db.entities.ScopedGrantAndName
import com.ustadmobile.core.controller.UstadListPresenter
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.ScopedGrantWithName
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter


class ScopedGrantListFragment(
): UstadListViewFragment<ScopedGrant, ScopedGrantWithName>(),
        ScopedGrantListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: ScopedGrantListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in ScopedGrantWithName>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = ScopedGrantListPresenter(requireContext(), arguments.toStringMap(), this,
            di, viewLifecycleOwner).withViewLifecycle()

        mDataRecyclerViewAdapter = ScopedGrantListRecyclerAdapter(mPresenter)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
            requireContext().getString(R.string.add))
        super.onViewCreated(view, savedInstanceState)
        fabManager?.text = requireContext().getString(R.string.permission)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.scopedGrantDao

}