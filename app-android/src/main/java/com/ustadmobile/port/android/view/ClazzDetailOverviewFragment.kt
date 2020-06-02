package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.FragmentClazzDetailOverviewBinding
import com.toughra.ustadmobile.databinding.ItemScheduleSimpleBinding
import com.ustadmobile.core.controller.ClazzDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.lib.db.entities.Schedule

class ClazzDetailOverviewFragment: UstadDetailFragment<ClazzWithDisplayDetails>(),
        ClazzDetailOverviewView, ClazzDetailFragmentEventHandler, Observer<PagedList<Schedule>> {

    private var mBinding: FragmentClazzDetailOverviewBinding? = null

    private var mPresenter: ClazzDetailOverviewPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var currentLiveData: LiveData<PagedList<Schedule>>? = null

    private var repo: UmAppDatabase? = null

    private var mScheduleListRecyclerAdapter: ScheduleRecyclerViewAdapter? = null

    class ScheduleRecyclerViewAdapter: PagedListAdapter<Schedule, ScheduleRecyclerViewAdapter.ScheduleViewHolder>(SCHEDULE_DIFF_UTIL) {

        class ScheduleViewHolder(val binding: ItemScheduleSimpleBinding): RecyclerView.ViewHolder(binding.root)

        override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
            holder.binding.schedule = getItem(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
            return ScheduleViewHolder(ItemScheduleSimpleBinding.inflate(LayoutInflater.from(parent.context),
                parent, false))
        }
    }

    override var scheduleList: DataSource.Factory<Int, Schedule>? = null
        set(value) {
            currentLiveData?.removeObserver(this)
            field = value
            val scheduleDao = repo?.scheduleDao ?: return
            currentLiveData = value?.asRepositoryLiveData(scheduleDao)
            currentLiveData?.observe(this, this)
        }

    override fun onChanged(t: PagedList<Schedule>?) {
        mScheduleListRecyclerAdapter?.submitList(t)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mScheduleListRecyclerAdapter = ScheduleRecyclerViewAdapter()
        mBinding = FragmentClazzDetailOverviewBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.fragmentClazzDetailOverviewScheduleRecyclerview.apply {
                adapter = mScheduleListRecyclerAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }

        repo = UmAccountManager.getRepositoryForActiveAccount(requireContext())
        mPresenter = ClazzDetailOverviewPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()), UmAccountManager.activeAccountLiveData)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.fragmentClazzDetailOverviewScheduleRecyclerview?.adapter = null
        mScheduleListRecyclerAdapter = null
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()

        //TODO: Set title here
    }

    override var entity: ClazzWithDisplayDetails? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazz = value
        }

    companion object {

        val SCHEDULE_DIFF_UTIL = object: DiffUtil.ItemCallback<Schedule>() {
            override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem.scheduleUid == newItem.scheduleUid
            }

            override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem == newItem
            }
        }
    }

}