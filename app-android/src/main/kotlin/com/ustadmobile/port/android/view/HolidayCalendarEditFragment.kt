package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentHolidaycalendarEditBinding
import com.toughra.ustadmobile.databinding.ItemHolidayBinding
import com.ustadmobile.core.controller.HolidayCalendarEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.HolidayCalendarEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.setEditFragmentTitle


interface HolidayCalendarEditActivityEventHandler {

    fun onClickEditHoliday(holiday: Holiday?)

    fun onClickNewHoliday()
}

class HolidayCalendarEditFragment() : UstadEditFragment<HolidayCalendar>(), HolidayCalendarEditView,
    HolidayCalendarEditActivityEventHandler {

    private var mBinding: FragmentHolidaycalendarEditBinding? = null

    private var mPresenter: HolidayCalendarEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, HolidayCalendar>?
        get() = mPresenter

    class HolidayRecyclerAdapter(val activityEventHandler: HolidayCalendarEditActivityEventHandler,
                                 var presenter: HolidayCalendarEditPresenter?): ListAdapter<Holiday, HolidayRecyclerAdapter.HolidayViewHolder>(DIFF_CALLBACK_HOLIDAY) {

        class HolidayViewHolder(val binding: ItemHolidayBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolidayViewHolder {
            val viewHolder = HolidayViewHolder(ItemHolidayBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.mActivity = activityEventHandler
            return viewHolder
        }

        override fun onBindViewHolder(holder: HolidayViewHolder, position: Int) {
            holder.binding.holiday = getItem(position)
        }
    }

    override var holidayList: DoorLiveData<List<Holiday>>? = null
        get() = field
        set(value) {
            field?.removeObserver(holidayObserver)
            field = value
            value?.observe(this, holidayObserver)
        }

    private var holidayRecyclerAdapter: HolidayRecyclerAdapter? = null

    private var holidayRecyclerView: RecyclerView? = null

    private val holidayObserver = Observer<List<Holiday>?> {
        t -> holidayRecyclerAdapter?.submitList(t)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentHolidaycalendarEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.activityEventHandler = this

        }

        holidayRecyclerView = rootView.findViewById(R.id.activity_holidaycalendar_holiday_recyclerview)
        holidayRecyclerAdapter = HolidayRecyclerAdapter(this, null)
        holidayRecyclerView?.adapter = holidayRecyclerAdapter
        holidayRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = HolidayCalendarEditPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()))
        holidayRecyclerAdapter?.presenter = mPresenter

        setEditFragmentTitle(R.string.holiday_calendar)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                Holiday::class.java) {
            val holiday = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleAddOrEditHoliday(holiday)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        holidayRecyclerView?.adapter = null
        holidayRecyclerView = null
        holidayRecyclerAdapter = null
        holidayList = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(mutableMapOf<String, String>().apply { mPresenter?.onSaveInstanceState(this) }.toBundle())
    }

    override fun onClickEditHoliday(holiday: Holiday?) {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(holiday, R.id.holiday_edit_dest, Holiday::class.java)
    }

    override fun onClickNewHoliday() = onClickEditHoliday(null)

    override var entity: HolidayCalendar? = null
        get() = field
        set(value) {
            field = value
            mBinding?.holidaycalendar = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

    override var loading: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.loading = value
        }


    companion object {

        val DIFF_CALLBACK_HOLIDAY = object: DiffUtil.ItemCallback<Holiday>() {
            override fun areItemsTheSame(oldItem: Holiday, newItem: Holiday): Boolean {
                return oldItem.holUid == newItem.holUid
            }

            override fun areContentsTheSame(oldItem: Holiday, newItem: Holiday): Boolean {
                return oldItem == newItem
            }
        }

    }


}