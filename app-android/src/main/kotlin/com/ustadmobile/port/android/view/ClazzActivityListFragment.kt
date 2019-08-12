package com.ustadmobile.port.android.view

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.paging.LivePagedListBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ClazzActivityListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ClazzActivityListView
import com.ustadmobile.core.view.ClazzActivityListView.Companion.ACTIVITY_BAR_CHART_AXIS_MINIMUM
import com.ustadmobile.core.view.ClazzActivityListView.Companion.ACTIVITY_BAR_CHART_HEIGHT
import com.ustadmobile.core.view.ClazzActivityListView.Companion.ACTIVITY_BAR_LABEL_DESC
import com.ustadmobile.core.view.ClazzActivityListView.Companion.CHART_DURATION_LAST_MONTH
import com.ustadmobile.core.view.ClazzActivityListView.Companion.CHART_DURATION_LAST_WEEK
import com.ustadmobile.core.view.ClazzActivityListView.Companion.CHART_DURATION_LAST_YEAR
import com.ustadmobile.lib.db.entities.ClazzActivityWithChangeTitle

import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedHashMap
import java.util.Objects

import ru.dimorinny.floatingtextbutton.FloatingTextButton

import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID


/**
 * This is the fragment that shows a list of activity done as well has charts at the top within a
 * Clazz.
 */
class ClazzActivityListFragment : UstadBaseFragment(), ClazzActivityListView {

    internal var rootContainer: View ?= null
    private var mRecyclerView: RecyclerView? = null

    private var mPresenter: ClazzActivityListPresenter? = null
    internal var barChart: BarChart ? = null

    internal var changesPresets: Array<String> ? = null
    internal var activityChangesSpinner: Spinner ? = null
    internal var negativeValue: HashMap<Float, Boolean>? = null
    private var lastWeekButton: Button? = null
    private var lastMonthButton: Button? = null
    private var lastYearButton: Button? = null

    internal var fab: FloatingTextButton ? = null

    fun hideEverythingInBarChart(barChart: BarChart): BarChart {

        barChart.getXAxis().setDrawLabels(true)
        barChart.getXAxis().setDrawGridLines(false)
        barChart.getAxisRight().setEnabled(false)
        barChart.getLegend().setEnabled(false)
        //Label Description
        barChart.getDescription().setEnabled(false)

        return barChart
    }

    internal inner class AttendanceBarDataSet(yVals: List<BarEntry>, label: String) : BarDataSet(yVals, label) {

        fun getColor(index: Int): Int {
            if (negativeValue != null) {
                if (negativeValue!!.containsKey(getEntryForIndex(index).getX())) {
                    if (negativeValue!![getEntryForIndex(index).getX()]!!) {
                        return mColors.get(2)
                    }
                }
            }

            return mColors.get(0)
        }
    }

    override fun updateActivityBarChart(dataMap: LinkedHashMap<Float, Float>) {
        val barDataEntries = ArrayList<BarEntry>()
        val dataMapIterator = dataMap.entries.iterator()
        negativeValue = HashMap()
        while (dataMapIterator.hasNext()) {
            val nextEntry = dataMapIterator.next()
            val thisBarKey = nextEntry.key
            var thisBarValue: Float? = nextEntry.value

            val anEntry = BarEntry(thisBarKey, thisBarValue)
            if (thisBarValue < 0) {
                anEntry.setIcon(AppCompatResources.getDrawable(Objects.requireNonNull(context),
                        R.drawable.ic_thumb_down_black_12dp))
            } else {
                anEntry.setIcon(AppCompatResources.getDrawable(Objects.requireNonNull(context),
                        R.drawable.ic_thumb_up_black_12dp))
            }
            if (thisBarValue < 0) {
                thisBarValue = -1 * thisBarValue!!
                anEntry.setY(thisBarValue)
                negativeValue!![thisBarKey] = true

            }

            barDataEntries.add(anEntry)
        }

        //Create Bar color
        val dataSetBar1 = AttendanceBarDataSet(barDataEntries,
                ACTIVITY_BAR_LABEL_DESC)
        dataSetBar1.setValueTextColor(Color.WHITE)
        dataSetBar1.setDrawValues(true)
        dataSetBar1.setValueFormatter { value, entry, dataSetIndex, viewPortHandler -> "" }

        dataSetBar1.setColors(ContextCompat.getColor(Objects.requireNonNull(context),
                R.color.traffic_green),
                ContextCompat.getColor(context, R.color.traffic_orange),
                ContextCompat.getColor(context, R.color.traffic_red))


        val barData = BarData(dataSetBar1)

        runOnUiThread {
            setUpCharts()
            barChart.setData(barData)
            barChart.setFitBars(true)
            barChart.invalidate()
            barChart.setDrawValueAboveBar(false)
        }

    }

    override fun setClazzActivityChangesDropdownPresets(presets: Array<String>) {
        this.changesPresets = presets
        val adapter = ArrayAdapter(Objects.requireNonNull(context),
                android.R.layout.simple_spinner_item, changesPresets)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        activityChangesSpinner.adapter = adapter
    }

    /**
     * Separated out method to set the charts up. Called on onCreateView
     */
    fun setUpCharts() {
        //Get the chart view
        barChart = rootContainer.findViewById<View>(R.id.fragment_clazz_activity_list_bar_chart)
        barChart.setMinimumHeight(dpToPx(ACTIVITY_BAR_CHART_HEIGHT))
        barChart = hideEverythingInBarChart(barChart)
        barChart.getAxisLeft().setAxisMinimum(ACTIVITY_BAR_CHART_AXIS_MINIMUM)
        barChart.setTouchEnabled(false)
    }


    /**
     * On Create of the View fragment . Part of Android's Fragment Override
     *
     * @param inflater              The inflator
     * @param container             The container
     * @param savedInstanceState    The saved instance
     * @return the root container
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        rootContainer = inflater.inflate(R.layout.fragment_clazz_activity_list, container, false)
        setHasOptionsMenu(true)

        mRecyclerView = rootContainer.findViewById(R.id.fragment_clazz_activity_list_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(context)
        mRecyclerView!!.setLayoutManager(mRecyclerLayoutManager)

        //Separated out Chart initialisation
        setUpCharts()

        //Record attendance FAB
        fab = rootContainer.findViewById(R.id.fragment_clazz_log_record_attendance_fab)
        fab.setOnClickListener { v -> mPresenter!!.goToNewClazzActivityEditActivity() }

        //Buttons
        lastWeekButton = rootContainer.findViewById(
                R.id.fragment_clazz_activity_list_bar_chart_selector_button_thisweek)
        lastMonthButton = rootContainer.findViewById(
                R.id.fragment_clazz_activity_list_bar_chart_selector_button_thismonth)
        lastYearButton = rootContainer.findViewById(
                R.id.fragment_clazz_activity_list_bar_chart_selector_button_lastyear)

        lastWeekButton!!.setOnClickListener { v ->
            mPresenter!!.getActivityDataAndUpdateCharts(CHART_DURATION_LAST_WEEK)
            getTintedDrawable(lastWeekButton!!.background, R.color.primary)
        }
        lastMonthButton!!.setOnClickListener { v ->
            mPresenter!!.getActivityDataAndUpdateCharts(CHART_DURATION_LAST_MONTH)
            getTintedDrawable(lastMonthButton!!.background, R.color.primary)
        }
        lastYearButton!!.setOnClickListener { v ->
            mPresenter!!.getActivityDataAndUpdateCharts(CHART_DURATION_LAST_YEAR)
            getTintedDrawable(lastYearButton!!.background, R.color.primary)
        }

        activityChangesSpinner = rootContainer.findViewById(R.id.fragment_clazz_activity_list_bar_chart_spinner)

        //Create the presenter and call its onCreate
        mPresenter = ClazzActivityListPresenter(this,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))


        activityChangesSpinner!!.onItemSelectedListener = object : AdapterView
        .OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPresenter!!.setClazzActivityChangeUid(id)
                //Default start to Last Week's data:
                lastWeekButton!!.callOnClick()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        lastWeekButton!!.callOnClick()

        //return container
        return rootContainer
    }

    fun getTintedDrawable(drawable: Drawable, color: Int): Drawable {
        var drawable = drawable
        drawable = DrawableCompat.wrap(drawable)
        val tintColor = ContextCompat.getColor(Objects.requireNonNull(context), color)
        DrawableCompat.setTint(drawable, tintColor)
        return drawable
    }

    /**
     * Removes background color from View's report button
     */
    override fun resetReportButtons() {
        runOnUiThread {
            getTintedDrawable(lastWeekButton!!.background, R.color.color_gray)
            getTintedDrawable(lastMonthButton!!.background, R.color.color_gray)
            getTintedDrawable(lastYearButton!!.background, R.color.color_gray)
        }
    }

    override fun setListProvider(factory: DataSource.Factory<Int, ClazzActivityWithChangeTitle>) {

        //Create a recycler adapter to set on the Recycler View.
        val recyclerAdapter = ClazzActivityListRecyclerAdapter(
                DIFF_CALLBACK, context, this, mPresenter, true)


        val data = LivePagedListBuilder(factory, 20).build()

        data.observe(this, ???({ recyclerAdapter.submitList() }))

        mRecyclerView!!.setAdapter(recyclerAdapter)
    }

    override fun setFABVisibility(visible: Boolean) {
        fab.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    override fun finish() {

    }

    companion object {

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment ClazzLogListFragment.
         */
        fun newInstance(clazzUid: Long): ClazzActivityListFragment {
            val fragment = ClazzActivityListFragment()
            val args = Bundle()
            args.putLong(ARG_CLAZZ_UID, clazzUid)
            fragment.arguments = args
            return fragment
        }

        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

        // ClassLogList's DIFF callback
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzActivityWithChangeTitle> = object : DiffUtil.ItemCallback<ClazzActivityWithChangeTitle>() {

            fun areItemsTheSame(oldItem: ClazzActivityWithChangeTitle, newItem: ClazzActivityWithChangeTitle): Boolean {
                return oldItem.clazzActivityUid == newItem.clazzActivityUid
            }

            fun areContentsTheSame(oldItem: ClazzActivityWithChangeTitle,
                                   newItem: ClazzActivityWithChangeTitle): Boolean {
                return oldItem == newItem
            }
        }
    }

}