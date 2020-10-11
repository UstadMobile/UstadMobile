package com.ustadmobile.port.android.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzDetailOverviewBinding
import com.toughra.ustadmobile.databinding.ItemScheduleSimpleBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ClazzDetailOverviewPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.lib.db.entities.Schedule
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

interface ClazzDetailOverviewEventListener {
    fun onClickClassCode(code: String?)
}

class ClazzDetailOverviewFragment: UstadDetailFragment<ClazzWithDisplayDetails>(),
        ClazzDetailOverviewView, ClazzDetailFragmentEventHandler, Observer<PagedList<Schedule>>,
        ClazzDetailOverviewEventListener {

    private var mBinding: FragmentClazzDetailOverviewBinding? = null

    private var mPresenter: ClazzDetailOverviewPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var currentLiveData: LiveData<PagedList<Schedule>>? = null

    private var repo: UmAppDatabase? = null

    private var mScheduleListRecyclerAdapter: ScheduleRecyclerViewAdapter? = null

    class ScheduleRecyclerViewAdapter: PagedListAdapter<Schedule,
            ScheduleRecyclerViewAdapter.ScheduleViewHolder>(SCHEDULE_DIFF_UTIL) {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mScheduleListRecyclerAdapter = ScheduleRecyclerViewAdapter()
        mBinding = FragmentClazzDetailOverviewBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
            it.fragmentClazzDetailOverviewScheduleRecyclerview.apply {
                adapter = mScheduleListRecyclerAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
        mBinding?.fragmentEventHandler = this

        val accountManager: UstadAccountManager by instance()
        repo = di.direct.on(accountManager.activeAccount).instance(tag = TAG_REPO)
        mPresenter = ClazzDetailOverviewPresenter(requireContext(), arguments.toStringMap(), this,
                 di, viewLifecycleOwner)
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


    override var entity: ClazzWithDisplayDetails? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazz = value
        }

    override var clazzCodeVisible: Boolean
        get() = mBinding?.clazzCodeVisible ?: false
        set(value) {
            mBinding?.clazzCodeVisible = value
        }

    override fun onClickClassCode(code: String?) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE)
                as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData(ClipData.newPlainText("link", code)))
        showSnackBar(requireContext().getString(R.string.copied_to_clipboard))
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