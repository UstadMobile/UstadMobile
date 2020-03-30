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
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ActivityHolidaycalendarEditBinding
import com.toughra.ustadmobile.databinding.ItemDateRangeBinding
import com.ustadmobile.core.controller.HolidayCalendarEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toBundle
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.HolidayCalendarEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.DateRange
import com.ustadmobile.lib.db.entities.HolidayCalendar

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.ustadmobile.port.android.util.ext.putExtraResultAsJson
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

    fun onClickNewDateRange()

    fun onClickEditDateRange(dateRange: DateRange?)
}

class HolidayCalendarEditActivity : UstadBaseActivity(), HolidayCalendarEditView,
        HolidayCalendarEditActivityEventHandler {

    private var rootView: ActivityHolidaycalendarEditBinding? = null

    private lateinit var mPresenter: HolidayCalendarEditPresenter



    class DateRangeRecyclerAdapter(val activityEventHandler: HolidayCalendarEditActivityEventHandler,
            var presenter: HolidayCalendarEditPresenter?): ListAdapter<DateRange, DateRangeRecyclerAdapter.DateRangeViewHolder>(DIFF_CALLBACK_DATERANGE) {

            class DateRangeViewHolder(val binding: ItemDateRangeBinding): RecyclerView.ViewHolder(binding.root)

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateRangeViewHolder {
                val viewHolder = DateRangeViewHolder(ItemDateRangeBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false))
                viewHolder.binding.mPresenter = presenter
                viewHolder.binding.mActivity = activityEventHandler
                return viewHolder
            }

            override fun onBindViewHolder(holder: DateRangeViewHolder, position: Int) {
                holder.binding.dateRange = getItem(position)
            }
        }

    override var dateRangeList: DoorLiveData<List<DateRange>>? = null
        get() = field
        set(value) {
            field?.removeObserver(dateRangeObserver)
            field = value
            value?.observe(this, dateRangeObserver)
        }

    private var dateRangeRecyclerAdapter: DateRangeRecyclerAdapter? = null

    private var dateRangeRecyclerView: RecyclerView? = null

    private val dateRangeObserver = Observer<List<DateRange>?> {
        t -> dateRangeRecyclerAdapter?.submitList(t)
    }

    override fun onClickEditDateRange(dateRange: DateRange?) {
//        prepareDateRangeEditCall {
//            val dateRangeCreated = it?.firstOrNull() ?: return@prepareDateRangeEditCall
//            mPresenter.handleAddOrEditDateRange(dateRangeCreated)
//        }.launchDateRangeEdit(dateRange)
    }

    override fun onClickNewDateRange() = onClickEditDateRange(null)

    /*
    TODO 1: Put these method signatures into the HolidayCalendarEditActivityEventHandler interface (at the top)
    fun onClickEditDateRange(dateRange: DateRange?)
    fun onClickNewDateRange()
    */

    /*
    TODO 2: put this into onCreate:
    dateRangeRecyclerView = findViewById(R.id.activity_DateRange_recycleradapter
    dateRangeRecyclerAdapter = DateRangeRecyclerAdapter(this, null)
    dateRangeRecyclerView?.adapter = dateRangeRecyclerAdapter
    dateRangeRecyclerView?.layoutManager = LinearLayoutManager(this)

    //After the presenter is created
    dateRangeRecyclerAdapter?.presenter = mPresenter
    */

    /*
    TODO 3
    Make a layout for the item in the recyclerview named item_DateRange (PS: convert DateRange to snake case).
    Use the Ustad Edit Screen 1-N ListItem XML (right click on res/layout, click new, and select the template)
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootView = DataBindingUtil.setContentView(this, R.layout.activity_holidaycalendar_edit)
        rootView?.activityEventHandler = this

        val toolbar = findViewById<Toolbar>(R.id.activity_holidaycalendar_edit_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mPresenter = HolidayCalendarEditPresenter(this, intent.extras.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(this),
                UmAccountManager.getRepositoryForActiveAccount(this))
        mPresenter.onCreate(savedInstanceState.toNullableStringMap())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(mutableMapOf<String, String>().apply { mPresenter.onSaveInstanceState(this) }.toBundle())
    }

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

    override fun finishWithResult(result: HolidayCalendar) {
        setResult(RESULT_OK, Intent().apply {
            putExtraResultAsJson(EXTRA_RESULT_KEY, listOf(result))
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

        val DIFF_CALLBACK_DATERANGE = object : DiffUtil.ItemCallback<DateRange>() {
            override fun areItemsTheSame(oldItem: DateRange, newItem: DateRange): Boolean {
                return oldItem.dateRangeUid == newItem.dateRangeUid
            }

            override fun areContentsTheSame(oldItem: DateRange, newItem: DateRange): Boolean {
                return oldItem == newItem
            }
        }

    }

}
