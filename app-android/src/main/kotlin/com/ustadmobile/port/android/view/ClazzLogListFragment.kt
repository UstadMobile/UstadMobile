package com.ustadmobile.port.android.view

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.google.android.material.snackbar.Snackbar
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ClazzLogListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ClassLogListView
import com.ustadmobile.core.view.ClassLogListView.Companion.ATTENDANCE_BAR_CHART_AXIS_MAXIMUM
import com.ustadmobile.core.view.ClassLogListView.Companion.ATTENDANCE_BAR_CHART_AXIS_MINIMUM
import com.ustadmobile.core.view.ClassLogListView.Companion.ATTENDANCE_BAR_CHART_HEIGHT
import com.ustadmobile.core.view.ClassLogListView.Companion.ATTENDANCE_BAR_LABEL_DESC
import com.ustadmobile.core.view.ClassLogListView.Companion.ATTENDANCE_LINE_CHART_COLOR
import com.ustadmobile.core.view.ClassLogListView.Companion.ATTENDANCE_LINE_CHART_HEIGHT
import com.ustadmobile.core.view.ClassLogListView.Companion.ATTENDANCE_LINE_LABEL_DESC
import com.ustadmobile.core.view.ClassLogListView.Companion.CHART_DURATION_LAST_MONTH
import com.ustadmobile.core.view.ClassLogListView.Companion.CHART_DURATION_LAST_WEEK
import com.ustadmobile.core.view.ClassLogListView.Companion.CHART_DURATION_LAST_YEAR
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.lib.db.entities.ClazzLogWithScheduleStartEndTimes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.dimorinny.floatingtextbutton.FloatingTextButton
import java.util.*
import kotlin.math.roundToInt

/**
 * ClazzLogListFragment Android fragment extends UstadBaseFragment
 */
class ClazzLogListFragment : UstadBaseFragment(), ClassLogListView {
    override val viewContext: Any
        get() = context!!

    internal lateinit var rootContainer: View
    private var mRecyclerView: RecyclerView? = null

    private var mPresenter: ClazzLogListPresenter? = null
    internal lateinit var lineChart: LineChart
    internal lateinit var barChart: HorizontalBarChart

    private var lastWeekButton: Button? = null
    private var lastMonthButton: Button? = null
    private var lastYearButton: Button? = null

    internal var fab: FloatingTextButton? = null

    internal var cl : View ? = null

    private var attendanceMessage: Snackbar? = null
    /**
     * Hides elements of MPAndroid Chart that we do not need as part of the Bar Chart in the
     * Attendance Log list fragment. Hides things as per UI intended (axis, labels, etc)
     *
     * @param barChart  The horizontal bar chart
     * @return  The horizontal bar chart with elements hidden
     */
    fun hideEverythingInBarChart(barChart: HorizontalBarChart): HorizontalBarChart {

        //Hide all lines from x, left and right
        //Top values on X Axis
        barChart.getXAxis().setEnabled(false)
        //Left Values
        barChart.getAxisLeft().setEnabled(false)
        //Right Values:
        barChart.getAxisRight().setEnabled(false)

        //Legend:
        barChart.getLegend().setEnabled(false)

        //Label Description
        barChart.getDescription().setEnabled(false)

        return barChart
    }

    /**
     * Hides elements of MpAndroid Chart that we do not need as part of the Line Chart in the
     * Attendance Log list fragment. Hides things as per UI intended (axis, labels, etc)
     *
     * @param lineChart The line chart
     * @return  The line chart with elements hidden.
     */
    fun hideEverythingInLineChart(lineChart: LineChart): LineChart {

        //We want the Left Axis grid (vertical lines)
        //lineChart.getAxisLeft().setDrawGridLines(false);

        //We don't want horizontal grid lines:
        lineChart.getXAxis().setDrawGridLines(false)

        //No need for legend:
        lineChart.getLegend().setEnabled(false)

        //We don't want the right label
        lineChart.getAxisRight().setDrawLabels(false)

        //We don't want the description label here
        lineChart.getDescription().setEnabled(false)

        return lineChart
    }

    /**
     * Custom Bar Data Set. The idea is any color logic can be applied here.
     */
    internal inner class AttendanceBarDataSet(yVals: List<BarEntry>, label: String)
        : BarDataSet(yVals, label) {
        override fun getEntryIndex(e: BarEntry?): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        //TODO:
//        fun getColor(index: Int): Int {
//            return mColors.get(index)
//        }

    }


    /**
     * Updates the line chart with attendance with data specified to it. This is directly drawing
     * on the MPAndroid Chart View in the Fragment.
     *
     * @param dataMap The hash map containing the values
     */
    override fun updateAttendanceLineChart(dataMap: LinkedHashMap<Float, Float>) {
        val lineDataEntries = ArrayList<Entry>()
        val lineDataEntriesZero = ArrayList<Entry>()
        for ((x, y) in dataMap) {
            val anEntry = Entry()
            anEntry.setX(x)
            anEntry.setY(y * 100)

            if (y > -1) { //If the value is meant to be 0
                lineDataEntries.add(anEntry)
            } else {
                //Not meant to be 0 - join adjacent lines (ie: not a school day/no ClazzLog)
                lineDataEntriesZero.add(Entry(x, 0F))
            }


        }

        //0 data set
        val dataSetLine0 = LineDataSet(lineDataEntriesZero, "Line 0 label")
        dataSetLine0.setColor(android.R.color.transparent)
        dataSetLine0.setValueTextColor(android.R.color.transparent)
        dataSetLine0.setDrawValues(false)
        dataSetLine0.setDrawCircles(false)

        //Create a line data set (one line)
        val dataSetLine1 = LineDataSet(lineDataEntries, ATTENDANCE_LINE_LABEL_DESC)
        dataSetLine1.setColor(Color.parseColor(ATTENDANCE_LINE_CHART_COLOR))
        dataSetLine1.setValueTextColor(Color.BLACK)
        dataSetLine1.setLineWidth(2.0f)
        //Don't want to see the values on the data points.
        dataSetLine1.setDrawValues(false)
        //Don't want to see the circles
        dataSetLine1.setDrawCircles(false)

        //dataSetLine1.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        //Add LineDataSet to LineData
        val lineData = LineData()
        lineData.addDataSet(dataSetLine1)
        //Add 0 line :
        lineData.addDataSet(dataSetLine0)

        //Update the lineChart on the UI thread (since this method is called via the Presenter)
        runOnUiThread (Runnable{
            setUpCharts()
            if (lineDataEntries.size > 0) {
                lineChart!!.setData(lineData)
                lineChart!!.invalidate()
            } else {
                lineChart!!.setData(null)
                lineChart!!.invalidate()
            }

        })
    }

    /**
     * Update the bar chart with attendance with data specified to it. This is directly drawing
     * on the MPAndroid Chart View in the Fragment.
     * @param dataMap the Hashmap containing the values.
     */
    override fun updateAttendanceBarChart(dataMap: LinkedHashMap<Float, Float>) {
        val barDataEntries = ArrayList<BarEntry>()
        for ((key, value) in dataMap) {
            val anEntry = BarEntry(key,
                    value * 100)
            barDataEntries.add(anEntry)
        }

        //Create Bar color
        val dataSetBar1 = AttendanceBarDataSet(barDataEntries,
                ATTENDANCE_BAR_LABEL_DESC)
        dataSetBar1.setValueTextColor(Color.GRAY)
        dataSetBar1.setDrawValues(true)
        dataSetBar1.setValueFormatter { value, entry, dataSetIndex, viewPortHandler -> "" +
                value.roundToInt() + "%" }

        val colors = intArrayOf(
                ContextCompat.getColor(context!!, R.color.traffic_green),
                ContextCompat.getColor(context!!, R.color.traffic_orange),
                ContextCompat.getColor(context!!, R.color.traffic_red))
        dataSetBar1.setColors(colors.toList())


        val barData = BarData(dataSetBar1)

        runOnUiThread (Runnable{
            setUpCharts()
            barChart.setData(barData)
            barChart.setFitBars(true)
            barChart.invalidate()
        })

    }

    /**
     * Separated out method to set the charts up. Called on onCreateView
     */
    fun setUpCharts() {
        //Get the chart view
        lineChart = rootContainer.findViewById(R.id.fragment_clazz_log_list_line_chart)
        lineChart.setMinimumHeight(dpToPx(ATTENDANCE_LINE_CHART_HEIGHT))
        lineChart = hideEverythingInLineChart(lineChart)
        lineChart.getAxisLeft().setValueFormatter({ value, axis -> (value.toInt()).toString() + "%" })
        lineChart.getXAxis().setValueFormatter({ value, axis -> (value.toInt()).toString() + "" })
        lineChart.setTouchEnabled(false)
        lineChart.getXAxis().setLabelCount(4, true)
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM)
        lineChart.getAxisLeft().setLabelCount(4, true)
        //lineChart.setExtraOffsets(0,-150,0,0);
        lineChart.getXAxis().setAxisMinimum(0F)
        lineChart.getAxisLeft().setAxisMinimum(0F)
        lineChart.getAxisLeft().setAxisMaximum(100F)

        barChart = rootContainer.findViewById(R.id.fragment_clazz_log_list_bar_chart)
        barChart.setMinimumHeight(dpToPx(ATTENDANCE_BAR_CHART_HEIGHT))
        barChart.setMinimumWidth(dpToPx(ATTENDANCE_BAR_CHART_HEIGHT))
        barChart = hideEverythingInBarChart(barChart)
        barChart.getAxisLeft().setAxisMaximum(ATTENDANCE_BAR_CHART_AXIS_MAXIMUM.toFloat())
        barChart.getAxisLeft().setAxisMinimum(ATTENDANCE_BAR_CHART_AXIS_MINIMUM.toFloat())
        barChart.setTouchEnabled(false)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        rootContainer = inflater.inflate(R.layout.fragment_clazz_log_list, container, false)
        setHasOptionsMenu(true)

        mRecyclerView = rootContainer!!.findViewById(R.id.fragment_class_log_list_recyclerview)
        val mRecyclerLayoutManager = LinearLayoutManager(context)
        mRecyclerView!!.setLayoutManager(mRecyclerLayoutManager)

        //Record attendance FAB
        fab = rootContainer!!.findViewById(R.id.fragment_class_log_record_attendance_fab)

        //Separated out Chart initialisation
        setUpCharts()

        //Create the presenter and call its onCreate
        mPresenter = ClazzLogListPresenter(this,
                UMAndroidUtil.bundleToMap(arguments), this)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))


        lastWeekButton = rootContainer!!.findViewById(
                R.id.fragment_clazz_log_list_line_chart_selector_button_thisweek)
        lastMonthButton = rootContainer!!.findViewById(
                R.id.fragment_clazz_log_list_line_chart_selector_button_thismonth)
        lastYearButton = rootContainer!!.findViewById(
                R.id.fragment_clazz_log_list_line_chart_selector_button_lastyear)

        lastWeekButton!!.setOnClickListener { v ->
            mPresenter!!.getAttendanceDataAndUpdateCharts(CHART_DURATION_LAST_WEEK)
            getTintedDrawable(lastWeekButton!!.background, R.color.primary)
            //lastWeekButton.getBackground().setTint(getResources().getColor(R.color.primary));
        }
        lastMonthButton!!.setOnClickListener { v ->
            mPresenter!!.getAttendanceDataAndUpdateCharts(CHART_DURATION_LAST_MONTH)
            getTintedDrawable(lastMonthButton!!.background, R.color.primary)
            //lastMonthButton.getBackground().setTint(getResources().getColor(R.color.primary));
        }
        lastYearButton!!.setOnClickListener { v ->
            mPresenter!!.getAttendanceDataAndUpdateCharts(CHART_DURATION_LAST_YEAR)
            getTintedDrawable(lastYearButton!!.background, R.color.primary)
            //lastYearButton.getBackground().setTint(getResources().getColor(R.color.primary));
        }

        //Default start to Last Week's data:
        lastWeekButton!!.callOnClick()

        //Take attendance fab click listener
        fab!!.setOnClickListener { v -> mPresenter!!.goToNewClazzLogDetailActivity() }

        return rootContainer
    }

    /**
     * Tints the drawable to the color. This method supports the Context compat tinting on drawables.
     *
     * @param drawable  The drawable to be tinted
     * @param color     The color of the tint
     */
    fun getTintedDrawable(drawable: Drawable, color: Int) {
        var drawable = drawable
        drawable = DrawableCompat.wrap(drawable)
        val tintColor = ContextCompat.getColor(context!!, color)
        DrawableCompat.setTint(drawable, tintColor)
    }

    /**
     * Removes background color from View's report button
     */
    override fun resetReportButtons() {
        runOnUiThread (Runnable{

            getTintedDrawable(lastWeekButton!!.background, R.color.color_gray)
            getTintedDrawable(lastMonthButton!!.background, R.color.color_gray)
            getTintedDrawable(lastYearButton!!.background, R.color.color_gray)

        })

    }

    override fun setFABVisibility(visible: Boolean) {
        if (visible) {
            fab!!.visibility = View.VISIBLE
        } else {
            fab!!.visibility = View.INVISIBLE
        }
    }

    override fun showMessage(message: String) {

        attendanceMessage = Snackbar
                .make(getActivity()!!.findViewById<View>(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
                .setAction(getText(R.string.dismiss).toString()) {
                    attendanceMessage!!.dismiss()
                }
        attendanceMessage!!.show()
    }
    override fun showMessage(messageId: Int) {
        val impl = UstadMobileSystemImpl.instance
        val message = impl.getString(messageId, context!!)
        showMessage(message)
    }

    override fun setClazzLogListProvider(factory : DataSource.Factory<Int,
            ClazzLogWithScheduleStartEndTimes>) {

        //Create a recycler adapter to set on the Recycler View.
        val recyclerAdapter = ClazzLogListRecyclerAdapter(DIFF_CALLBACK, context!!, this,
                mPresenter!!, false)

        val data = LivePagedListBuilder(factory, 20).build()

        //Observe the data:
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            data.observe(thisP,
                    Observer<PagedList<ClazzLogWithScheduleStartEndTimes>> {
                        recyclerAdapter.submitList(it) })
            mRecyclerView!!.setAdapter(recyclerAdapter)
        }
    }

    companion object {

        /**
         * Generates a new Fragment for a page fragment
         *
         * @return A new instance of fragment ClazzLogListFragment.
         */
        fun newInstance(clazzUid: Long): ClazzLogListFragment {
            val fragment = ClazzLogListFragment()
            val args = Bundle()
            args.putLong(ARG_CLAZZ_UID, clazzUid)
            fragment.arguments = args
            return fragment
        }

        fun newInstance(args: Bundle): ClazzLogListFragment {
            val fragment = ClazzLogListFragment()
            fragment.arguments = args
            return fragment
        }

        /**
         * Converts dp to pixels (used in MPAndroid charts)
         *
         * @param dp    dp number
         * @return      pixels number
         */
        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

        // ClassLogList's DIFF callback
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzLogWithScheduleStartEndTimes> = object
            : DiffUtil.ItemCallback<ClazzLogWithScheduleStartEndTimes>() {

            override fun areItemsTheSame(oldItem: ClazzLogWithScheduleStartEndTimes,
                                         newItem: ClazzLogWithScheduleStartEndTimes): Boolean {
                return oldItem.clazzLogUid == newItem.clazzLogUid
            }

            override fun areContentsTheSame(oldItem: ClazzLogWithScheduleStartEndTimes,
                                            newItem: ClazzLogWithScheduleStartEndTimes): Boolean {
                return oldItem == newItem
            }
        }
    }

}
