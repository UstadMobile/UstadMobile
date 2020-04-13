package com.ustadmobile.port.android.view

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.ustadmobile.port.android.view.util.CrudEditActivityResultContract
import com.ustadmobile.core.controller.UstadSingleEntityPresenter
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityHolidaycalendarEditBinding
import com.toughra.ustadmobile.databinding.ItemHolidayBinding
import com.ustadmobile.core.controller.HolidayCalendarEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.HolidayCalendarEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.lib.db.entities.HolidayCalendar

import com.ustadmobile.port.android.util.ext.putExtraResultAsJson
import com.ustadmobile.port.android.view.ext.setEditActivityTitle
import com.ustadmobile.port.android.view.util.AbstractCrudActivityResultContract.Companion.EXTRA_RESULT_KEY


fun ComponentActivity.prepareHolidayCalendarEditCall(callback: (List<HolidayCalendar>?) -> Unit) = prepareCall(CrudEditActivityResultContract(this, HolidayCalendar::class.java,
        HolidayCalendarEditActivity::class.java, HolidayCalendar::umCalendarUid)) {
    callback.invoke(it)
}

fun ActivityResultLauncher<CrudEditActivityResultContract.CrudEditInput<HolidayCalendar>>.launchHolidayCalendarEdit(schedule: HolidayCalendar?, extraArgs: Map<String, String> = mapOf()) {
    //TODO: Set PersistenceMode to JSON or DB here
    launch(CrudEditActivityResultContract.CrudEditInput(schedule,
            UstadSingleEntityPresenter.PersistenceMode.JSON, extraArgs))
}



interface HolidayCalendarEditActivityEventHandler {

    fun onClickEditHoliday(holiday: Holiday?)

    fun onClickNewHoliday()
}

class HolidayCalendarEditActivity : UstadBaseActivity(), HolidayCalendarEditView,
        HolidayCalendarEditActivityEventHandler {

    private var rootView: ActivityHolidaycalendarEditBinding? = null

    private lateinit var mPresenter: HolidayCalendarEditPresenter


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootView = DataBindingUtil.setContentView(this, R.layout.activity_holidaycalendar_edit)
        rootView?.activityEventHandler = this

        val toolbar = findViewById<Toolbar>(R.id.activity_holidaycalendar_edit_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setEditActivityTitle(R.string.holiday_calendar)

        holidayRecyclerView = findViewById(R.id.activity_holidaycalendar_holiday_recyclerview)
        holidayRecyclerAdapter = HolidayRecyclerAdapter(this, null)
        holidayRecyclerView?.adapter = holidayRecyclerAdapter
        holidayRecyclerView?.layoutManager = LinearLayoutManager(this)

        mPresenter = HolidayCalendarEditPresenter(this, intent.extras.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(this),
                UmAccountManager.getRepositoryForActiveAccount(this))
        holidayRecyclerAdapter?.presenter= mPresenter
        mPresenter.onCreate(savedInstanceState.toNullableStringMap())
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(mutableMapOf<String, String>().apply { mPresenter.onSaveInstanceState(this) }.toBundle())
    }

    override fun onClickEditHoliday(holiday: Holiday?) {
        prepareHolidayEditCall {
            val holidayCreated = it?.firstOrNull() ?: return@prepareHolidayEditCall
            mPresenter.handleAddOrEditHoliday(holidayCreated)
        }.launchHolidayEdit(holiday)
    }

    override fun onClickNewHoliday() = onClickEditHoliday(null)

    override var entity: HolidayCalendar? = null
        get() = field
        set(value) {
            field = value
            rootView?.holidaycalendar = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            rootView?.fieldsEnabled = value
        }

    override fun finishWithResult(result: List<HolidayCalendar>) {
//        setResult(RESULT_OK, Intent().apply {
//            putExtraResultAsJson(EXTRA_RESULT_KEY, listOf(result))
//        })
//        finish()
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
                val entityVal = rootView?.holidaycalendar ?: return false
                mPresenter.handleClickSave(entityVal)
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
