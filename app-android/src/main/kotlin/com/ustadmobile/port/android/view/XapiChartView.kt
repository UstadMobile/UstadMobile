package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.ustadmobile.core.controller.XapiReportOptions
import com.ustadmobile.core.db.dao.StatementDao


class XapiChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {


    fun setChartData(chartData: List<StatementDao.ReportData>, options: XapiReportOptions) {
        removeAllViewsInLayout()
        var chart = createChart(chartData, options)
        addView(chart)
    }

    private fun createChart(chartData: List<StatementDao.ReportData>, options: XapiReportOptions): View? {

        if (options.chartType == XapiReportOptions.BAR_CHART) {

            val barChart = BarChart(context)
            val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            barChart.layoutParams = params

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
            barChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            barChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            barChart.legend.setDrawInside(true)

            //Label Description
            barChart.description.isEnabled = false

            barChart.setTouchEnabled(false)


            //get a list of distinct subgroups
            val distinctSubgroups = chartData.distinctBy { it.subgroup }.map { it.subgroup }
            val groupedByXAxis = chartData.groupBy { it.xAxis }

            var secondList = mutableListOf<MutableList<BarEntry>>()
            distinctSubgroups.forEachIndexed { idx, subGroup ->
                var xAxisList = mutableListOf<BarEntry>()
                groupedByXAxis.keys.forEach { xAxisKey ->
                    val barReportData = groupedByXAxis[xAxisKey]?.firstOrNull { it.subgroup == subGroup }
                    val barValue = barReportData?.yAxis ?: 0
                    var barEntry = BarEntry((idx).toFloat(), barValue.toFloat())
                    xAxisList.add(barEntry)
                }
                secondList.add(xAxisList)
            }

            val barSpace = 0.02f
            val groupSpace = 0.3f

            var barData = BarData()
            secondList.forEachIndexed { idx, it ->
                var barDataSet = BarDataSet(it, idx.toString())
                barData.barWidth = (1/groupedByXAxis.keys.size.toFloat())// (1 - groupSpace) / it.size - barSpace
                barData.addDataSet(barDataSet)
            }

            val months = arrayOf("Male", "Female", "Other")
            val xAxis = barChart.xAxis
            barChart.axisLeft.axisMinimum = 0f
            xAxis.valueFormatter = IndexAxisValueFormatter(months)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setCenterAxisLabels(true)
            xAxis.granularity = 1f
            xAxis.isGranularityEnabled = true
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = groupedByXAxis.keys.size.toFloat()

            barChart.data = barData
            barChart.xAxis.mAxisMaximum = 0 + barChart.barData.getGroupWidth(groupSpace, barSpace) * xAxis.axisMaximum
            barChart.groupBars(0f, groupSpace, barSpace)
            barChart.invalidate()

            return barChart
        } else if (options.chartType == XapiReportOptions.LINE_GRAPH) {


            val lineChart = LineChart(context)
            val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            lineChart.layoutParams = params

            return lineChart
        }
        return null
    }


}