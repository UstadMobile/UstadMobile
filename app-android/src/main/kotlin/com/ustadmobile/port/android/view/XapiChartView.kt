package com.ustadmobile.port.android.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.ustadmobile.core.controller.XapiReportOptions
import com.ustadmobile.core.db.dao.StatementDao


class XapiChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    var colorList = listOf("#009688", "#FF9800", "#2196F3", "#f44336", "#673AB7", "#607D8B", "#E91E63", "#9C27B0", "#795548", "9E9E9E", "#4CAF50")

    fun setChartData(chartData: List<StatementDao.ReportData>, options: XapiReportOptions, xAxisLabels: Map<String, String>, subgroupLabels: Map<String, String>) {
        removeAllViewsInLayout()
        var chart = createChart(chartData, options, xAxisLabels, subgroupLabels)
        addView(chart)
    }

    private fun createChart(chartData: List<StatementDao.ReportData>, options: XapiReportOptions,
                            xAxisLabels: Map<String, String>, subgroupLabels: Map<String, String>): View? {

        var xAxisLabelList: MutableSet<String> = mutableSetOf()
        var subgroupList: MutableSet<String> = mutableSetOf()
        //get a list of distinct subgroups
        val distinctSubgroups = chartData.distinctBy { it.subgroup }.map { it.subgroup }
        val groupedByXAxis = chartData.groupBy { it.xAxis }

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

            var secondList = mutableListOf<MutableList<BarEntry>>()
            distinctSubgroups.forEachIndexed { idx, subGroup ->
                var xAxisList = mutableListOf<BarEntry>()
                subgroupList.add(subgroupLabels[subGroup] ?: error(""))
                groupedByXAxis.keys.forEach { xAxisKey ->
                    xAxisLabelList.add(xAxisLabels[xAxisKey] ?: error(""))
                    val barReportData = groupedByXAxis[xAxisKey]?.firstOrNull { it.subgroup == subGroup }
                    val barValue = barReportData?.yAxis ?: 0f
                    var barEntry = BarEntry((idx).toFloat(), barValue)
                    xAxisList.add(barEntry)
                }
                secondList.add(xAxisList)
            }

            val sizeOfX = subgroupList.size
            val barSpace = 0.01f
            val groupSpace = 0.08f
            val barWidth = (1 - groupSpace) / sizeOfX - barSpace

            var barData = BarData()
            barData.barWidth = barWidth
            secondList.forEachIndexed { idx, it ->
                var barDataSet = BarDataSet(it, subgroupList.elementAt(idx))
                barDataSet.color = Color.parseColor(colorList[idx])
                barDataSet.setDrawValues(false)
                barData.addDataSet(barDataSet)
            }

            val xAxis = barChart.xAxis
            barChart.axisLeft.axisMinimum = 0f
            barChart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabelList)
            barChart.data = barData


            barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            barChart.xAxis.setCenterAxisLabels(true)
            barChart.xAxis.granularity = 1f
            xAxis.labelRotationAngle = -45f
            barChart.xAxis.isGranularityEnabled = true
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = xAxisLabelList.size.toFloat()
            barChart.xAxis.mAxisMaximum = 0 + barChart.barData.getGroupWidth(groupSpace, barSpace) * xAxis.axisMaximum
            if(sizeOfX > 1){
                barChart.groupBars(0f, groupSpace, barSpace)
            }

            barChart.invalidate()

            return barChart
        } else if (options.chartType == XapiReportOptions.LINE_GRAPH) {

            val lineChart = LineChart(context)
            val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            lineChart.layoutParams = params


            lineChart.xAxis.isEnabled = true
            lineChart.xAxis.setDrawGridLines(false)
            lineChart.xAxis.setDrawLabels(true)

            //Left Values
            lineChart.axisLeft.isEnabled = true
            lineChart.axisLeft.setDrawTopYLabelEntry(true)

            //Right Values:
            lineChart.axisRight.isEnabled = false

            //Legend:
            lineChart.legend.isEnabled = true
            lineChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            lineChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            lineChart.legend.setDrawInside(true)

            //Label Description
            lineChart.description.isEnabled = false
            lineChart.setTouchEnabled(false)


            var secondList = mutableListOf<MutableList<Entry>>()
            distinctSubgroups.forEach { subGroup ->
                var xAxisList = mutableListOf<Entry>()
                subgroupList.add(subgroupLabels[subGroup] ?: error(""))
                groupedByXAxis.keys.forEachIndexed { idx, xAxisKey ->
                    xAxisLabelList.add(xAxisLabels[xAxisKey] ?: error(""))
                    val barReportData = groupedByXAxis[xAxisKey]?.firstOrNull { it.subgroup == subGroup }
                    val barValue = barReportData?.yAxis ?: 0f
                    var barEntry = Entry((idx).toFloat(), barValue)
                    xAxisList.add(barEntry)
                }
                secondList.add(xAxisList)
            }


            var barData = LineData()
            secondList.forEachIndexed { idx, it ->
                var barDataSet = LineDataSet(it, subgroupList.elementAt(idx))
                barDataSet.axisDependency = YAxis.AxisDependency.LEFT
                barDataSet.color = Color.parseColor(colorList[idx])
                barDataSet.setDrawValues(false)
                barData.addDataSet(barDataSet)
            }
            lineChart.data = barData


            val xAxis = lineChart.xAxis
            lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.valueFormatter =  IndexAxisValueFormatter(xAxisLabelList)
            xAxis.labelRotationAngle = -45f
            xAxis.axisMinimum = 0f

            lineChart.invalidate()

            return lineChart
        }
        return null
    }


}