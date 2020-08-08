package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.MergeAdapter
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ClazzWorkDetailProgressListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.dao.ClazzWorkDao
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ClazzWorkDetailProgressListView
import com.ustadmobile.core.view.ListViewAddMode
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.ClazzMemberWithClazzWorkProgress
import com.ustadmobile.lib.db.entities.ClazzWorkWithMetrics

class ClazzWorkDetailProgressListFragment : UstadListViewFragment<ClazzMemberWithClazzWorkProgress,
        ClazzMemberWithClazzWorkProgress>(), ClazzWorkDetailProgressListView{

    private var mPresenter: ClazzWorkDetailProgressListPresenter? = null

    override var autoMergeRecyclerViewAdapter: Boolean = false

    private var metricsRecyclerAdapter : ClazzWorkMetricsRecyclerAdapter? = null

    private var metricsLiveData: LiveData<PagedList<ClazzWorkWithMetrics>>? = null

    private val metricsObserver = Observer<PagedList<ClazzWorkWithMetrics>?> {
        t -> metricsRecyclerAdapter?.submitList(t)
    }

    override val listPresenter: UstadListPresenter<*, in ClazzMemberWithClazzWorkProgress>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = ClazzWorkDetailProgressListPresenter(requireContext(),
                UMAndroidUtil.bundleToMap(arguments), this, di, this)
        addMode = ListViewAddMode.NONE

        metricsRecyclerAdapter = ClazzWorkMetricsRecyclerAdapter(null, false)
        mDataRecyclerViewAdapter = ClazzWorkProgressListRecyclerAdapter(mPresenter)

        mMergeRecyclerViewAdapter = MergeAdapter(metricsRecyclerAdapter,
                mDataRecyclerViewAdapter)
        mDataBinding?.fragmentListRecyclerview?.adapter = mMergeRecyclerViewAdapter

        return view
    }

    override fun onResume() {
        super.onResume()
        addMode = ListViewAddMode.NONE
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.student_progress)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzWorkDao

    override var clazzWorkWithMetrics: DataSource.Factory<Int, ClazzWorkWithMetrics>? = null
        get() = field
        set(value) {
            metricsLiveData?.removeObserver(metricsObserver)
            metricsLiveData = value?.asRepositoryLiveData(ClazzWorkDao)
            field = value
            metricsLiveData?.observe(viewLifecycleOwner, metricsObserver)
        }

    companion object {

        val DU_CLAZZWORKWITHMETRICS = object: DiffUtil.ItemCallback<ClazzWorkWithMetrics>() {
            override fun areItemsTheSame(oldItem: ClazzWorkWithMetrics,
                                         newItem: ClazzWorkWithMetrics): Boolean {
                return oldItem.clazzWorkUid == newItem.clazzWorkUid
            }

            override fun areContentsTheSame(oldItem: ClazzWorkWithMetrics,
                                            newItem: ClazzWorkWithMetrics): Boolean {
                return oldItem == newItem
            }
        }
    }
}