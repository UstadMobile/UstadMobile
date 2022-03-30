package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.CourseTerminologyListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CourseTerminologyListView
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter


class CourseTerminologyListFragment(): UstadListViewFragment<CourseTerminology, CourseTerminology>(),
        CourseTerminologyListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: CourseTerminologyListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in CourseTerminology>?
        get() = mPresenter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = CourseTerminologyListPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner).withViewLifecycle()

        mDataRecyclerViewAdapter = CourseTerminologyListRecyclerAdapter(mPresenter)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
            requireContext().getString(R.string.add_new_terminology))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ustadFragmentTitle = requireContext().getString(R.string.select_terminology)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout)
            mPresenter?.handleClickAddNewItem()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.courseTerminologyDao

}