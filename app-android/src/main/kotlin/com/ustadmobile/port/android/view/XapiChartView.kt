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

            barChart.xAxis.isEnabled = true;
            barChart.getXAxis().setDrawGridLines(false);
            barChart.getXAxis().setDrawLabels(true);

            //Left Values
            barChart.getAxisLeft().setEnabled(true);
            barChart.getAxisLeft().setDrawTopYLabelEntry(true);

            //Right Values:
            barChart.getAxisRight().setEnabled(false);

            //Legend:
            barChart.getLegend().setEnabled(true);
            barChart.getLegend().setPosition(Legend.LegendPosition.RIGHT_OF_CHART)

            //Label Description
            barChart.getDescription().setEnabled(false)

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
                    var barEntry = BarEntry((idx + 1).toFloat(), barValue.toFloat())
                    xAxisList.add(barEntry)
                }
                secondList.add(xAxisList)
            }


            var barData = BarData()
            secondList.forEachIndexed { idx, it ->
                var barDataSet = BarDataSet(it, idx.toString())
                barData.barWidth = 0.15f
                barData.addDataSet(barDataSet)
            }

            val months = arrayOf("Male", "Female", "Other")
            val xAxis = barChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(months)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setCenterAxisLabels(true)

            val barSpace = 0.02f
            val groupSpace = 0.3f
            val groupCount = groupedByXAxis.keys.size

            barChart.data = barData
            barChart.xAxis.axisMinimum = 0.toFloat()
            barChart.xAxis.axisMaximum = 0 + barChart.barData.getGroupWidth(groupSpace, barSpace) * groupCount
            barChart.groupBars(0.toFloat(), groupSpace, barSpace)

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