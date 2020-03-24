package com.ustadmobile.port.android.view

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.BR
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityClazzEdit2Binding
import com.toughra.ustadmobile.databinding.ItemSchedule2Binding
import com.ustadmobile.core.controller.ClazzEdit2Presenter
import com.ustadmobile.core.controller.ScheduleEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.GetResultMode
import com.ustadmobile.core.view.ListViewMode
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendar
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.Schedule
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

interface ClazzEdit2ActivityEventHandler {

    fun showNewScheduleDialog()

    fun showEditScheduleDialog(schedule: Schedule)

    fun showHolidayCalendarPicker()

    fun handleClickTimeZone()

}

class ClazzEdit2Activity : UstadBaseActivity(), ClazzEdit2View, Observer<List<Schedule?>>,
        ClazzEdit2ActivityEventHandler, ScheduleEditPresenter.ScheduleEditDoneListener,
        OnTimeZoneSelectedListener {

    private var rootView: ActivityClazzEdit2Binding? = null

    private lateinit var mPresenter: ClazzEdit2Presenter

    private var scheduleRecyclerAdapter: ScheduleRecyclerAdapter? = null

    private var scheduleRecyclerView: RecyclerView? = null

    class ScheduleRecyclerAdapter(val activityEventHandler: ClazzEdit2ActivityEventHandler,
        var presenter: ClazzEdit2Presenter?): ListAdapter<Schedule, ScheduleRecyclerAdapter.ScheduleViewHolder>(DIFF_CALLBACK_SCHEDULE) {

        class ScheduleViewHolder(val binding: ItemSchedule2Binding): RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
            val viewHolder = ScheduleViewHolder(ItemSchedule2Binding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            viewHolder.binding.mPresenter = presenter
            viewHolder.binding.mActivity = activityEventHandler
            return viewHolder
        }

        override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
            holder.binding.schedule = getItem(position)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootView = DataBindingUtil.setContentView(this, R.layout.activity_clazz_edit2)
        rootView?.activityEventHandler = this

        val toolbar = findViewById<Toolbar>(R.id.activity_clazz_edit_toolbar)
        toolbar.setTitle(R.string.class_setup)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        scheduleRecyclerAdapter = ScheduleRecyclerAdapter(this, null)
        scheduleRecyclerView = findViewById(R.id.activity_clazz_edit_schedule_recyclerview)
        scheduleRecyclerView?.adapter = scheduleRecyclerAdapter
        scheduleRecyclerView?.layoutManager = LinearLayoutManager(this)

        mPresenter = ClazzEdit2Presenter(this, intent.extras.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(this),
                UmAccountManager.getRepositoryForActiveAccount(this))
        scheduleRecyclerAdapter?.presenter = mPresenter
        mPresenter.onCreate(savedInstanceState.toNullableStringMap())

        GlobalScope.launch {
            UmAccountManager.getRepositoryForActiveAccount(this).holidayCalendarDao
                    .replaceList(listOf(HolidayCalendar().apply { umCalendarName = "Test 1"}))
        }
    }

    override fun onChanged(t: List<Schedule?>?) {
        scheduleRecyclerAdapter?.submitList(t)
    }

    override fun showNewScheduleDialog() {
        ScheduleEditDialogFragment().show(supportFragmentManager, TAG_SCHEDULE_EDIT_DIALOG)
    }

    override fun showEditScheduleDialog(schedule: Schedule) {
        val scheduleEditDialog = ScheduleEditDialogFragment.newInstance(schedule)
        scheduleEditDialog.show(supportFragmentManager, TAG_SCHEDULE_EDIT_DIALOG)
    }

    override fun showHolidayCalendarPicker() {
        prepareCall(HolidayCalendarActivityResultContract(this)) {
            if(it != null) {
                entity?.holidayCalendar = it[0]
                rootView?.clazz = rootView?.clazz
            }
        }.launch(GetResultMode.FROMLIST)
    }

    override fun handleClickTimeZone() {
        val timezoneDialog = TimeZoneListDialogFragment.newInstance(
                rootView?.clazz?.clazzTimeZone ?: "")
        timezoneDialog.show(supportFragmentManager, TAG_TIMEZONE_DIALOG)
    }

    override fun onScheduleEditDone(schedule: Schedule, requestCode: Int) {
        mPresenter.handleAddOrEditSchedule(schedule)
    }

    override fun onTimeZoneSelected(timeZone: TimeZone) {
        rootView?.clazz?.clazzTimeZone = timeZone.id
        rootView?.clazz = rootView?.clazz
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(mutableMapOf<String, String>().apply { mPresenter.onSaveInstanceState(this) }.toBundle())
    }

    override var clazzSchedules: DoorMutableLiveData<List<Schedule>>? = null
        get() = field
        set(value) {
            field?.removeObserver(this)
            field = value
            value?.observe(this, this)
        }

    override var entity: ClazzWithHolidayCalendar? = null
        get() = field
        set(value) {
            field = value
            rootView?.clazz = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            rootView?.fieldsEnabled = value
        }

    override fun finishWithResult(result: ClazzWithHolidayCalendar) {
        TODO("Not yet implemented")
    }

    override var loading: Boolean = false
        get() = field
        set(value) {
            field = value
            rootView?.loading = value
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_done -> {
                val selectedClazz = rootView?.clazz ?: return false
                mPresenter.handleClickSave(selectedClazz)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_done, menu)
        return true
    }

    companion object {
        const val TAG_SCHEDULE_EDIT_DIALOG = "scheduleEdit"

        const val TAG_TIMEZONE_DIALOG = "timezoneDialog"

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
