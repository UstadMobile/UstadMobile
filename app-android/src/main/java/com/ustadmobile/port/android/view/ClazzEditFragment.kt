package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzEditBinding
import com.toughra.ustadmobile.databinding.ItemScheduleBinding
import com.ustadmobile.core.controller.BitmaskEditPresenter
import com.ustadmobile.core.controller.ClazzEdit2Presenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.LongWrapper
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToEditEntity
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList

interface ClazzEdit2ActivityEventHandler {

    fun showNewScheduleDialog()

    fun showEditScheduleDialog(schedule: Schedule)

    fun showHolidayCalendarPicker()

    fun handleClickTimeZone()

    fun showFeaturePicker()

    fun handleClickSchool()

}

class ClazzEditFragment() : UstadEditFragment<ClazzWithHolidayCalendarAndSchool>(), ClazzEdit2View,
        ClazzEdit2ActivityEventHandler {

    private var mDataBinding: FragmentClazzEditBinding? = null

    private var mPresenter: ClazzEdit2Presenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzWithHolidayCalendarAndSchool>?
        get() = mPresenter

    private var scheduleRecyclerAdapter: ScheduleRecyclerAdapter? = null

    private var scheduleRecyclerView: RecyclerView? = null

    private val scheduleObserver = Observer<List<Schedule>?> {
        t -> scheduleRecyclerAdapter?.submitList(t)
    }

    override val viewContext: Any
        get() = requireContext()

    override var clazzSchedules: DoorMutableLiveData<List<Schedule>>? = null
        set(value) {
            field?.removeObserver(scheduleObserver)
            field = value
            value?.observe(this, scheduleObserver)
        }

    class ScheduleRecyclerAdapter(val activityEventHandler: ClazzEdit2ActivityEventHandler,
                                  var presenter: ClazzEdit2Presenter?): ListAdapter<Schedule, ScheduleRecyclerAdapter.ScheduleViewHolder>(DIFF_CALLBACK_SCHEDULE) {

        class ScheduleViewHolder(val binding: ItemScheduleBinding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
            val viewHolder = ScheduleViewHolder(ItemScheduleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.mActivity = activityEventHandler
            return viewHolder
        }

        override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
            holder.binding.schedule = getItem(position)
        }
    }

    override var entity: ClazzWithHolidayCalendarAndSchool? = null
        get() = field
        set(value) {
            mDataBinding?.clazz = value
            field = value
        }


    override var fieldsEnabled: Boolean = true
        set(value) {
            field = value
            mDataBinding?.fieldsEnabled = value
        }

    override var loading: Boolean = false
        set(value) {
            field = value
            //TODO: set this on activity
        }



    override fun showNewScheduleDialog() {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(null, R.id.schedule_edit_dest, Schedule::class.java)
    }

    override fun showEditScheduleDialog(schedule: Schedule) {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(schedule, R.id.schedule_edit_dest, Schedule::class.java)
    }

    override fun showHolidayCalendarPicker() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(HolidayCalendar::class.java, R.id.holidaycalendar_list_dest)
    }

    override fun handleClickSchool() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(School::class.java, R.id.home_schoollist_dest)
    }

    override fun showFeaturePicker() {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(LongWrapper(entity?.clazzFeatures ?: 0L), R.id.bitmask_edit_dest,
                LongWrapper::class.java, destinationResultKey = CLAZZ_FEATURES_KEY)
    }

    override fun handleClickTimeZone() {
        onSaveStateToBackStackStateHandle()
        navigateToPickEntityFromList(TimeZoneEntity::class.java, R.id.timezoneentity_list_dest)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View

        mDataBinding = FragmentClazzEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.activityEventHandler = this
            it.featuresBitmaskFlags = BitmaskEditPresenter.FLAGS_AVAILABLE
        }

        scheduleRecyclerAdapter = ScheduleRecyclerAdapter(this, null)
        scheduleRecyclerView = rootView.findViewById(R.id.activity_clazz_edit_schedule_recyclerview)
        scheduleRecyclerView?.adapter = scheduleRecyclerAdapter
        scheduleRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = ClazzEdit2Presenter(requireContext(), arguments.toStringMap(), this@ClazzEditFragment,
                 di, viewLifecycleOwner)
        scheduleRecyclerAdapter?.presenter = mPresenter
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.clazz)

        val navController = findNavController()

        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                Schedule::class.java) {
            val schedule = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleAddOrEditSchedule(schedule)
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                HolidayCalendar::class.java) {
            val holidayCalendar = it.firstOrNull() ?: return@observeResult
            entity?.holidayCalendar = holidayCalendar
            entity?.clazzHolidayUMCalendarUid = holidayCalendar.umCalendarUid
            mDataBinding?.clazz = entity
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                School::class.java) {
            val school = it.firstOrNull() ?: return@observeResult
            entity?.clazzSchoolUid = school.schoolUid
            entity?.school = school
            mDataBinding?.fragmentClazzEditSchoolText?.setText(school.schoolName)
            mDataBinding?.clazz = entity
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
            TimeZoneEntity::class.java) {
            val timeZone = it.firstOrNull() ?: return@observeResult
            entity?.clazzTimeZone = timeZone.id
            mDataBinding?.clazz = entity
        }

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                LongWrapper::class.java, CLAZZ_FEATURES_KEY) {
            val clazzFeatures = it.firstOrNull() ?: return@observeResult
            entity?.clazzFeatures = clazzFeatures.longValue
            mDataBinding?.clazz = entity
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDataBinding = null
        scheduleRecyclerView = null
        scheduleRecyclerAdapter = null
        clazzSchedules = null
    }

    companion object {

        const val CLAZZ_FEATURES_KEY = "clazzFeatures"

        val DIFF_CALLBACK_SCHEDULE: DiffUtil.ItemCallback<Schedule> = object: DiffUtil.ItemCallback<Schedule>() {
            override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem.scheduleUid == newItem.scheduleUid
            }

            override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
                return oldItem == newItem
            }
        }
    }
}