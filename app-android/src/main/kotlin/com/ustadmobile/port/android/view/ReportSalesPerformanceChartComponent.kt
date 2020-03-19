package com.ustadmobile.port.android.view

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ReportBarChartComponentView
import com.ustadmobile.lib.db.entities.ReportSalesPerformance
import kotlinx.coroutines.Runnable
import java.util.*

class ReportSalesPerformanceChartComponent : LinearLayout,
        ReportBarChartComponentView {
    override val viewContext: Any
        get() = context!!


    internal lateinit var barChart: BarChart
    internal lateinit var mContext: Context

    constructor(context: Context) : super(context) {
        mContext = context
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {}

    override fun setChartData(dataSet: List<Any>) {
        runOnUiThread (Runnable {
            removeAllViews()
            barChart = createSalesBarChart(dataSet)
            addView(barChart!!)
        })

    }

    private fun createSalesBarChart(dataSet: List<Any>): BarChart {

        var barChart = BarChart(context)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        barChart.layoutParams = params

        //barChart = setUpCharts(barChart);
        barChart = hideEverythingInBarChart(barChart)

        val yAxisValues: Array<String>

        val yAxisValueList = ArrayList<String>()

        //TODO: Have unlimited variations
        val barColorsList = ArrayList<String>()
        barColorsList.add("#ffa600")
        barColorsList.add("#488f31")
        barColorsList.add("#bc5090")
        barColorsList.add("#58508d")
        barColorsList.add("#003f5c")
        val barColors = barColorsList.toTypedArray()


        //Map of Location Uid (Group 1) and BarEntry list
        val locationToBarEntriesMap = HashMap<Long, ArrayList<BarEntry>>()
        val allDateOccurences = ArrayList<String>()

        //Map of LocationUid -> Name for Label searching
        val locationUidToName = HashMap<Long, String>()

        //Get all date occurrences from the data. (This will be x axis - 2nd group)
        for (data in dataSet) {
            val entry = data as ReportSalesPerformance
            if (!allDateOccurences.contains(entry.firstDateOccurence)) {
                allDateOccurences.add(entry.firstDateOccurence!!)
            }
        }

        //Build every location's value plotting data.
        //Loop over every data output.
        for (i in dataSet.indices) {

            //Get Report data:
            val entry = dataSet[i] as ReportSalesPerformance
            //Sale amount
            val saleAmount = entry.saleAmount
            //Get date of occurrence
            val saleOccurrence = entry.firstDateOccurence
            //Get location
            val locationUid = entry.locationUid

            //Build the Location Uid, Location Name for future lookup.
            var locationName = entry.locationName
            if (!locationUidToName.containsKey(locationUid)) {
                if(locationName == null){
                    locationName = ""
                }
                locationUidToName[locationUid] = locationName!!
            }

            //Get the Group 1's Bar Entry list. Its either a new one or one already made.
            val locationBarEntries: ArrayList<BarEntry>?
            if (locationToBarEntriesMap.containsKey(locationUid)) {
                locationBarEntries = locationToBarEntriesMap[locationUid]
            } else {
                locationBarEntries = ArrayList()
            }

            if (!allDateOccurences.contains(saleOccurrence)) {
                //Add it
                allDateOccurences.add(saleOccurrence!!)
            }

            //Get index of where in the date occurrence this data belongs
            val index = allDateOccurences.indexOf(saleOccurrence) + 1

            //Add entry in this index
            locationBarEntries!!.add(BarEntry(index.toFloat(), saleAmount.toFloat()))

            //END: update bar entries.
            locationToBarEntriesMap[locationUid] = locationBarEntries

        }

        //Buld the bar Data set for every Location
        val data = BarData()
        var colorPos = 0
        for (barEntry in locationToBarEntriesMap.keys) {
            //Get location name
            var locationName = locationUidToName[barEntry]
            if(locationName.isNullOrBlank()){
                locationName = UstadMobileSystemImpl.instance.getString(MessageID.not_set, context)
            }

            //Get entries (values plotted)
            val locationEntry = locationToBarEntriesMap[barEntry]!!


            //Create BarDataSet
            val barDataSet = BarDataSet(locationEntry, locationName)

            //Color the bar
            val barColor: String
            if (barColors.size > colorPos) {
                barColor = barColors[colorPos]
            } else {
                barColor = barColors[1]
            }
            barDataSet.color = Color.parseColor(barColor)
            colorPos++

            //Add to data :
            data.addDataSet(barDataSet)

        }

        //Get yAxis for chart of date occurrences
        for (everyDateOccurrence in allDateOccurences) {
            val prettyDate = UMCalendarUtil.getPrettyDateSuperSimpleFromLong(
                    UMCalendarUtil.convertYYYYMMddToLong(everyDateOccurrence), null)
            yAxisValueList.add(prettyDate)
        }

        yAxisValues = yAxisValueList.toTypedArray()

        barChart.data = data

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(yAxisValues)
        barChart.axisLeft.axisMinimum = 0f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setCenterAxisLabels(true)
        xAxis.isGranularityEnabled = true

        val barSpace = 0.02f
        val groupSpace = 0.3f
        val groupCount = yAxisValueList.size

        data.barWidth = 0.15f
        barChart.xAxis.axisMinimum = 0f
        barChart.xAxis.axisMaximum = 0 + barChart.barData.getGroupWidth(groupSpace, barSpace) * groupCount

        if (colorPos > 1) {
            barChart.groupBars(0f, groupSpace, barSpace)
        }

        //Hide values on top of every bar
        barChart.barData.setDrawValues(false)

        return barChart
    }

    private fun hideEverythingInBarChart(barChart: BarChart): BarChart {

        //Hide all lines from x, left and right
        //Top values on X Axis
        barChart.xAxis.isEnabled = true
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.setDrawLabels(true)

        //Left Values
        barChart.axisLeft.isEnabled = true
        barChart.axisLeft.setDrawTopYLabelEntry(true)

        //Right Values:
        barChart.axisRight.isEnabled = false

        //Legend:
        barChart.legend.isEnabled = true
        barChart.legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP)
        barChart.legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT)
        //barChart.legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART)

        //Label Description
        barChart.description.isEnabled = false

        barChart.setTouchEnabled(false)

        return barChart
    }

    override fun runOnUiThread(r: Runnable?) {
        (mContext as Activity).runOnUiThread(r)
    }

}
