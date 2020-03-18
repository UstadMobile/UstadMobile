package com.ustadmobile.staging.port.android.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.HolidayCalendarDetailPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.HolidayCalendarDetailView
import com.ustadmobile.lib.db.entities.DateRange
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.port.android.view.UstadBaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.util.*

class HolidayCalendarDetailActivity : UstadBaseActivity(), HolidayCalendarDetailView {

    private var toolbar: Toolbar? = null
    private var mPresenter: HolidayCalendarDetailPresenter? = null
    private var mRecyclerView: RecyclerView? = null
    private var addRangeCL: ConstraintLayout? = null
    private var title: EditText? = null


    /**
     * This method catches menu buttons/options pressed in the toolbar. Here it is making sure
     * the activity goes back when the back button is pressed.
     *
     * @param item The item selected
     * @return true if accounted for
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting layout:
        setContentView(R.layout.activity_holiday_calendar_detail)

        //Toolbar:
        toolbar = findViewById(R.id.activity_holiday_calendar_detail_toolbar)
        toolbar!!.title = getText(R.string.new_holiday)
        setSupportActionBar(toolbar)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        //RecyclerView
        mRecyclerView = findViewById(
                R.id.activity_holiday_calendar_detail_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.layoutManager = mRecyclerLayoutManager

        addRangeCL = findViewById(R.id.activity_holiday_calendar_detail_add_cl)

        title = findViewById(R.id.activity_holiday_calendar_detail_name)
        title!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                mPresenter!!.updateCalendarName(s.toString())
            }
        })

        //Call the Presenter
        mPresenter = HolidayCalendarDetailPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))

        addRangeCL!!.setOnClickListener { v -> mPresenter!!.handleAddDateRange() }

        //FAB and its listener
        val fab = findViewById<FloatingTextButton>(R.id.activity_holiday_calendar_detail_fab)
        fab.setOnClickListener { v -> mPresenter!!.handleClickDone() }

    }

    override fun setListProvider(factory: DataSource.Factory<Int, DateRange>) {
        val recyclerAdapter = HolidayCalendarDetailRecyclerAdapter(DIFF_CALLBACK, mPresenter!!, this,
                applicationContext)

        // get the provider, set , observe, etc.

        val data = LivePagedListBuilder(factory, 20).build()

        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<DateRange>> { recyclerAdapter.submitList(it) })
        }

        //set the adapter
        mRecyclerView!!.adapter = recyclerAdapter
    }

    override fun updateCalendarOnView(updatedCalendar: HolidayCalendar) {

        var calendarName: String? = ""

        if (updatedCalendar != null) {
            if (updatedCalendar.umCalendarName != null) {
                calendarName = updatedCalendar.umCalendarName
            }
        }

        val finalCalendarName = calendarName
        runOnUiThread { title!!.setText(finalCalendarName) }

    }

    companion object {

        /**
         * The DIFF CALLBACK
         */
        val DIFF_CALLBACK: DiffUtil.ItemCallback<DateRange> = object : DiffUtil.ItemCallback<DateRange>() {
            override fun areItemsTheSame(oldItem: DateRange,
                                         newItem: DateRange): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: DateRange,
                                            newItem: DateRange): Boolean {
                return oldItem.dateRangeUid == newItem.dateRangeUid
            }
        }
    }
}
