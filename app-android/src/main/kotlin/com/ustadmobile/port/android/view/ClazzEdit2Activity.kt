package com.ustadmobile.port.android.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityClazzEdit2Binding
import com.toughra.ustadmobile.databinding.ItemScheduleBinding
import com.ustadmobile.core.controller.ClazzEdit2Presenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzWithHolidayCalendar
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.port.android.util.ext.putExtraResultAsJson
import com.ustadmobile.port.android.view.ext.setEditActivityTitle
import com.ustadmobile.port.android.view.util.AbstractCrudActivityResultContract.Companion.EXTRA_RESULT_KEY
import java.util.*

interface ClazzEdit2ActivityEventHandler {

    fun showNewScheduleDialog()

    fun showEditScheduleDialog(schedule: Schedule)

    fun showHolidayCalendarPicker()

    fun handleClickTimeZone()

}

class ClazzEdit2Activity : UstadBaseActivity(), ClazzEdit2View, Observer<List<Schedule?>>,
        ClazzEdit2ActivityEventHandler, OnTimeZoneSelectedListener {

    private var rootView: ActivityClazzEdit2Binding? = null

    private lateinit var mPresenter: ClazzEdit2Presenter

    private var scheduleRecyclerAdapter: ScheduleRecyclerAdapter? = null

    private var scheduleRecyclerView: RecyclerView? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootView = DataBindingUtil.setContentView(this, R.layout.activity_clazz_edit2)
        rootView?.activityEventHandler = this

        val toolbar = findViewById<Toolbar>(R.id.activity_clazz_edit_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setEditActivityTitle(R.string.clazz)
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
    }

    override fun onChanged(t: List<Schedule?>?) {
        scheduleRecyclerAdapter?.submitList(t)
    }

    override fun showNewScheduleDialog() {
//        prepareScheduleEditCall {
//            val scheduleCreated = it?.firstOrNull() ?: return@prepareScheduleEditCall
//            mPresenter.handleAddOrEditSchedule(scheduleCreated)
//        }.launchScheduleEdit(null)
    }

    override fun showEditScheduleDialog(schedule: Schedule) {
//        prepareScheduleEditCall {
//            val scheduleCreated = it?.firstOrNull() ?: return@prepareScheduleEditCall
//            mPresenter.handleAddOrEditSchedule(scheduleCreated)
//        }.launchScheduleEdit(schedule)
    }

    override fun showHolidayCalendarPicker() {
//        prepareHolidayCalendarPickFromListCall {
//            if(it != null) {
//                entity?.holidayCalendar = it[0]
//                entity?.clazzHolidayUMCalendarUid = it[0].umCalendarUid
//                rootView?.clazz = rootView?.clazz
//            }
//        }.launch(mapOf())
    }

    override fun handleClickTimeZone() {
        val timezoneDialog = TimeZoneListDialogFragment.newInstance(
                rootView?.clazz?.clazzTimeZone ?: "")
        timezoneDialog.show(supportFragmentManager, TAG_TIMEZONE_DIALOG)
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

    override fun finishWithResult(result: List<ClazzWithHolidayCalendar>) {
        setResult(RESULT_OK, Intent().apply {
            putExtraResultAsJson(EXTRA_RESULT_KEY, result)
        })
        finish()
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
