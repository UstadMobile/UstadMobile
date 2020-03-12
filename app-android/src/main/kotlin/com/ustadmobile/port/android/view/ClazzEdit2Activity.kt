package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.toughra.ustadmobile.R
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.databinding.ActivityClazzEdit2Binding
import com.ustadmobile.core.controller.ClazzEdit2Presenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Schedule

interface ClazzEdit2ActivityEventHandler {

    fun handleClickAddSchedule()

    fun handleClickEditSchedule(schedule: Schedule)

}

class ClazzEdit2Activity : UstadBaseActivity(), ClazzEdit2View, Observer<List<Schedule?>>,
        ClazzEdit2ActivityEventHandler, ScheduleEditDialogFragment.ScheduleEditDialogFragmentListener {

    private var rootView: ActivityClazzEdit2Binding? = null

    private lateinit var mPresenter: ClazzEdit2Presenter

    private var scheduleRecyclerAdapter: ListAdapter<Schedule, ScheduleRecyclerAdapter.ScheduleViewHolder>? = null

    private var scheduleRecyclerView: RecyclerView? = null

    class ScheduleRecyclerAdapter(): ListAdapter<Schedule, ScheduleRecyclerAdapter.ScheduleViewHolder>(DIFF_CALLBACK_SCHEDULE) {

        class ScheduleViewHolder(view: View): RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
            TODO("Not yet implemented")
        }

        override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
            TODO("Not yet implemented")
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
        scheduleRecyclerAdapter = ScheduleRecyclerAdapter()
        scheduleRecyclerView = findViewById(R.id.activity_clazz_edit_schedule_recyclerview)
        scheduleRecyclerView?.adapter = scheduleRecyclerAdapter

        mPresenter = ClazzEdit2Presenter(this, intent.extras.toStringMap(), this,
                UmAccountManager.getActiveDatabase(this),
                UmAccountManager.getRepositoryForActiveAccount(this))
        mPresenter.onCreate(savedInstanceState.toStringMap())
    }

    override fun onChanged(t: List<Schedule?>?) {
        scheduleRecyclerAdapter?.submitList(t)
    }

    override fun handleClickAddSchedule() {
        val scheduleEditDialog = ScheduleEditDialogFragment()
        scheduleEditDialog.show(supportFragmentManager, TAG_SCHEDULE_EDIT_DIALOG)
    }

    override fun handleClickEditSchedule(schedule: Schedule) {

    }

    override fun onScheduleDone(schedule: Schedule?) {

    }

    override var clazzSchedules: DoorMutableLiveData<List<Schedule>>? = null
        get() = field
        set(value) {
            field?.removeObserver(this)
            field = value
            value?.observe(this, this)
        }

    override var clazz: Clazz? = null
        get() = field
        set(value) {
            field = value
            rootView?.clazz = clazz
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            rootView?.fieldsEnabled = value
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
                mPresenter.handleClickDone(selectedClazz)
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
