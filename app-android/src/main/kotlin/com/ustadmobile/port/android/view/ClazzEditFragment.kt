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
import com.ustadmobile.core.controller.ClazzEdit2Presenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultGson
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.ext.setAllFromMap
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendar
import com.ustadmobile.lib.db.entities.Schedule

class ClazzEditFragment() : UstadBaseFragment(), ClazzEdit2View, ClazzEdit2ActivityEventHandler {

    private var mDataBinding: FragmentClazzEditBinding? = null

    private var mPresenter: ClazzEdit2Presenter? = null

    private var scheduleRecyclerAdapter: ScheduleRecyclerAdapter? = null

    private var scheduleRecyclerView: RecyclerView? = null

    private val scheduleObserver = Observer<List<Schedule>?> {
        t -> scheduleRecyclerAdapter?.submitList(t)
    }

    override val viewContext: Any
        get() = requireContext()

    override var clazzSchedules: DoorMutableLiveData<List<Schedule>>? = null
        get() = field
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

    override var entity: ClazzWithHolidayCalendar? = null
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
        val navController = findNavController()
        mutableMapOf<String, String>().apply {
            mPresenter?.onSaveInstanceState(this)
            navController.currentBackStackEntry?.savedStateHandle?.setAllFromMap(this)
        }

        findNavController().navigate(R.id.schedule_edit_dest)
    }

    override fun showEditScheduleDialog(schedule: Schedule) {
        val navController = findNavController()
        mutableMapOf<String, String>().apply {
            mPresenter?.onSaveInstanceState(this)
            navController.currentBackStackEntry?.savedStateHandle?.setAllFromMap(this)
        }

        findNavController().navigate(R.id.schedule_edit_dest, Bundle().apply {
            putEntityAsJson(UstadEditView.ARG_ENTITY_JSON, schedule)
        })
    }

    override fun showHolidayCalendarPicker() {
        TODO("Not yet implemented")
    }

    override fun handleClickTimeZone() {
        TODO("Not yet implemented")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View


        mDataBinding = FragmentClazzEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.activityEventHandler = this
        }

        scheduleRecyclerAdapter = ScheduleRecyclerAdapter(this, null)
        scheduleRecyclerView = rootView.findViewById(R.id.activity_clazz_edit_schedule_recyclerview)
        scheduleRecyclerView?.adapter = scheduleRecyclerAdapter
        scheduleRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = ClazzEdit2Presenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()))
        scheduleRecyclerAdapter?.presenter = mPresenter
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        val savedStateMap = savedStateHandle?.toStringMap()

        mPresenter?.onCreate(savedStateMap)
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String?>("schedule")
                ?.observe(this, Observer {
                    val jsonStr = it ?: return@Observer
                    mPresenter?.handleAddOrEditSchedule(defaultGson().fromJson(jsonStr, Schedule::class.java))
                    navController.currentBackStackEntry?.savedStateHandle?.set("schedule", null)
                })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDataBinding = null
        scheduleRecyclerView = null
        scheduleRecyclerAdapter = null
        clazzSchedules = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(mutableMapOf<String, String>().apply { mPresenter?.onSaveInstanceState(this) }.toBundle())
    }

    override fun finishWithResult(result: ClazzWithHolidayCalendar) {
        //pass this as per https://developer.android.com/guide/navigation/navigation-programmatic
    }

    companion object {

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