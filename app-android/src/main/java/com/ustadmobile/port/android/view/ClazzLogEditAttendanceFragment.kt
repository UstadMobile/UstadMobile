package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzLogEditAttendanceBinding
import com.toughra.ustadmobile.databinding.ItemClazzLogAttendanceRecordEditBinding
import com.toughra.ustadmobile.databinding.ItemClazzLogEditAttendanceDateheaderBinding
import com.toughra.ustadmobile.databinding.ItemClazzLogEditAttendanceMarkallBinding
import com.ustadmobile.core.controller.ClazzLogEditAttendancePresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzLogEditAttendanceView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson

import java.util.*

interface ClazzLogEditAttendanceFragmentEventHandler {

}

class ClazzLogEditAttendanceFragment: UstadEditFragment<ClazzLog>(), ClazzLogEditAttendanceView, ClazzLogEditAttendanceFragmentEventHandler {

    internal var mBinding: FragmentClazzLogEditAttendanceBinding? = null

    private var mPresenter: ClazzLogEditAttendancePresenter? = null

    class MarkAllRecyclerAdapter(var presenter: ClazzLogEditAttendancePresenter?) : ListAdapter<Int, MarkAllRecyclerAdapter.MarkAllViewHolder>(DIFFUTIL_INT) {

        class MarkAllViewHolder(var binding: ItemClazzLogEditAttendanceMarkallBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkAllViewHolder {
            return MarkAllViewHolder(ItemClazzLogEditAttendanceMarkallBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: MarkAllViewHolder, position: Int) {
            val resourceIds = RESOURCE_ID_MAP[getItem(position)] ?: return
            val markAllValue = getItem(position)
            holder.binding.itemClazzLogEditAttendanceMarkallText.text = holder.itemView.context.getText(resourceIds.first)
            holder.binding.itemClazzLogEditAttendanceMarkallIcon.setImageResource(resourceIds.second)
            holder.binding.root.setOnClickListener {
                presenter?.handleClickMarkAll(markAllValue)
            }
        }

        companion object {
            val RESOURCE_ID_MAP = mapOf(
                    ClazzLogAttendanceRecord.STATUS_ATTENDED to Pair(R.string.mark_all_present, R.drawable.ic_checkbox_multiple_marked),
                    ClazzLogAttendanceRecord.STATUS_ABSENT to Pair(R.string.mark_all_absent, R.drawable.ic_checkbox_blank))
        }
    }

    class DateHeaderRecyclerViewAdapter(): ListAdapter<Pair<Long, TimeZone>, DateHeaderRecyclerViewAdapter.DateHeaderViewHolder>(DIFFUTIL_TIME_TIMEZONEPAIR) {

        class DateHeaderViewHolder(var binding: ItemClazzLogEditAttendanceDateheaderBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DateHeaderViewHolder(
            ItemClazzLogEditAttendanceDateheaderBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false))

        override fun onBindViewHolder(holder: DateHeaderViewHolder, position: Int) {
            val item = getItem(position)
            holder.binding.date = item.first
            holder.binding.timeZone = item.second
        }
    }

    class ClazzLogAttendanceRecordRecyclerAdapter(val activityEventHandler: ClazzLogEditAttendanceFragmentEventHandler,
            var presenter: ClazzLogEditAttendancePresenter?): ListAdapter<ClazzLogAttendanceRecordWithPerson, ClazzLogAttendanceRecordRecyclerAdapter.ClazzLogAttendanceRecordViewHolder>(DIFFUTIL_CLAZZATTENDANCERECORD) {

            class ClazzLogAttendanceRecordViewHolder(val binding: ItemClazzLogAttendanceRecordEditBinding): RecyclerView.ViewHolder(binding.root)

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzLogAttendanceRecordViewHolder {
                val viewHolder = ClazzLogAttendanceRecordViewHolder(ItemClazzLogAttendanceRecordEditBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false))
                viewHolder.binding.mPresenter = presenter
                return viewHolder
            }

            override fun onBindViewHolder(holder: ClazzLogAttendanceRecordViewHolder, position: Int) {
                holder.binding.clazzLogAttendanceRecordWithPerson = getItem(position)
                holder.binding.attendanceRecordStatusMap = STATUS_MAP
            }
        }

    override var clazzLogAttendanceRecordList: DoorMutableLiveData<List<ClazzLogAttendanceRecordWithPerson>>? = null
        get() = field
        set(value) {
            field?.removeObserver(clazzLogAttendanceRecordObserver)
            field = value
            value?.observe(this, clazzLogAttendanceRecordObserver)
        }

    private var clazzLogAttendanceRecordRecyclerAdapter: ClazzLogAttendanceRecordRecyclerAdapter? = null

    private var clazzLogAttendanceRecordRecyclerView: RecyclerView? = null

    private val clazzLogAttendanceRecordObserver = Observer<List<ClazzLogAttendanceRecordWithPerson>?> {
        t -> clazzLogAttendanceRecordRecyclerAdapter?.submitList(t)
    }


    override val mEditPresenter: UstadEditPresenter<*, ClazzLog>?
        get() = mPresenter

    private var mMarkAllRecyclerAdapter: MarkAllRecyclerAdapter? = null

    override var clazzLogTimezone: String? = null

    private var mDateHeaderRecylerViewAdapter: DateHeaderRecyclerViewAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzLogEditAttendanceBinding.inflate(inflater, container, false).also {
            rootView = it.root
            clazzLogAttendanceRecordRecyclerView = it.clazzLogEditRecyclerView
            it.clazzLogEditRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        }

        mPresenter = ClazzLogEditAttendancePresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)

        mMarkAllRecyclerAdapter = MarkAllRecyclerAdapter(mPresenter).also {
            it.submitList(listOf(ClazzLogAttendanceRecord.STATUS_ATTENDED, ClazzLogAttendanceRecord.STATUS_ABSENT))
        }

        clazzLogAttendanceRecordRecyclerAdapter = ClazzLogAttendanceRecordRecyclerAdapter(
                this, mPresenter)
        mDateHeaderRecylerViewAdapter = DateHeaderRecyclerViewAdapter()
        clazzLogAttendanceRecordRecyclerView?.adapter = MergeAdapter(mDateHeaderRecylerViewAdapter,
                mMarkAllRecyclerAdapter, clazzLogAttendanceRecordRecyclerAdapter)

        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var entity: ClazzLog? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzLog = value
            val timeZoneVal = clazzLogTimezone
            if(value != null && timeZoneVal != null)
                mDateHeaderRecylerViewAdapter?.submitList(listOf(value.logDate to TimeZone.getTimeZone(timeZoneVal)))
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

    companion object {
        val DIFFUTIL_CLAZZATTENDANCERECORD = object: DiffUtil.ItemCallback<ClazzLogAttendanceRecordWithPerson>() {
            override fun areItemsTheSame(oldItem: ClazzLogAttendanceRecordWithPerson, newItem: ClazzLogAttendanceRecordWithPerson): Boolean {
                return oldItem.clazzLogAttendanceRecordUid == newItem.clazzLogAttendanceRecordUid
            }

            override fun areContentsTheSame(oldItem: ClazzLogAttendanceRecordWithPerson, newItem: ClazzLogAttendanceRecordWithPerson): Boolean {
                return oldItem === newItem
            }
        }

        val DIFFUTIL_INT = object: DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }
        }

        val DIFFUTIL_TIME_TIMEZONEPAIR = object: DiffUtil.ItemCallback<Pair<Long, TimeZone>>() {
            override fun areItemsTheSame(oldItem: Pair<Long, TimeZone>, newItem: Pair<Long, TimeZone>): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Pair<Long, TimeZone>, newItem: Pair<Long, TimeZone>): Boolean {
                return oldItem == newItem
            }
        }

        val STATUS_MAP = mapOf(ClazzLogAttendanceRecord.STATUS_ATTENDED to R.id.present_button,
            ClazzLogAttendanceRecord.STATUS_ABSENT to R.id.absent_button,
            ClazzLogAttendanceRecord.STATUS_PARTIAL to R.id.late_button)
    }

}