package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.CourseGroupSetListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CourseGroupSetListView
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter


class CourseGroupSetListFragment(): UstadListViewFragment<CourseGroupSet, CourseGroupSet>(),
        CourseGroupSetListView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var individualRecyclerViewAdapter: IndividualCourseGroupRecyclerAdapter? = null
    private var mPresenter: CourseGroupSetListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in CourseGroupSet>?
        get() = mPresenter

    override var autoMergeRecyclerViewAdapter: Boolean = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = CourseGroupSetListPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner).withViewLifecycle()

        individualRecyclerViewAdapter = IndividualCourseGroupRecyclerAdapter(mPresenter)
        mDataRecyclerViewAdapter = CourseGroupSetListRecyclerAdapter(mPresenter)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
            requireContext().getString(R.string.new_group_set))

        mMergeRecyclerViewAdapter = ConcatAdapter(mUstadListHeaderRecyclerViewAdapter,
            individualRecyclerViewAdapter, mDataRecyclerViewAdapter)

        mDataBinding?.fragmentListRecyclerview?.adapter = mMergeRecyclerViewAdapter


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.text = requireContext().getString(R.string.groups)
        takeIf { arguments?.containsKey(CourseGroupSetListView.ARG_SHOW_INDIVIDUAL) == true }
            ?.ustadFragmentTitle = requireContext().getString(R.string.submission_type)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        if(view?.id == R.id.item_createnew_layout)
            mPresenter?.handleClickCreateNewFab()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
        individualRecyclerViewAdapter = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.courseGroupSetDao

    override var individualList: List<CourseGroupSet>? = null
        set(value) {
            field = value
            individualRecyclerViewAdapter?.submitList(value)
            mUstadListHeaderRecyclerViewAdapter?.newItemVisible = value?.isNotEmpty() ?: false
        }

}