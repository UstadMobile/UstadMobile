package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.ustadmobile.core.controller.XapiReportOptions
import com.ustadmobile.core.db.dao.StatementDao


class XapiChartView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {


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

            //get a list of distinct subgroups
            var finalList = mutableListOf<MutableList<BarEntry>>()
            val distinctSubgroups = chartData.distinctBy { it.subgroup }.map { it.subgroup }
            val groupedByXAxis = chartData.groupBy { it.xAxis }
            groupedByXAxis.keys.forEach { xAxisKey ->
                var xAxisList = mutableListOf<BarEntry>()
                distinctSubgroups.forEachIndexed { idx, subGroup ->
                    val barReportData = groupedByXAxis[xAxisKey]?.firstOrNull { it.subgroup == subGroup }
                    val barValue = barReportData?.yAxis ?: 0
                    var barEntry = BarEntry(idx.toFloat(), barValue.toFloat())
                    xAxisList.add(barEntry)
                }
                finalList.add(xAxisList)
            }


            var barData = BarData()
            finalList.forEachIndexed{ idx, it ->
                var barDataSet = BarDataSet(it, chartData[idx].xAxis)
                barData.addDataSet(barDataSet)
            }

            barChart.data = barData
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