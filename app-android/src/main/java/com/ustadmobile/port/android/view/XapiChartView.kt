package com.ustadmobile.port.android.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.ustadmobile.core.util.ext.ChartData
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.port.android.util.graph.asValueFormatter


class XapiChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    var colorList = listOf("#009688", "#FF9800", "#2196F3", "#f44336", "#673AB7", "#607D8B", "#E91E63", "#9C27B0", "#795548", "9E9E9E", "#4CAF50")

    fun setChartData(chartData: ChartData?) {
        if (chartData == null) {
            return
        }
        removeAllViewsInLayout()
        val chart = createChart(chartData)
        addView(chart)
    }

    private fun createChart(chartData: ChartData): View {

        val combinedChart = CombinedChart(context).apply{
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            xAxis.apply {
                setDrawGridLines(false)
                setDrawLabels(true)
                setCenterAxisLabels(false)
                isEnabled = true
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                labelRotationAngle = -45f
                isGranularityEnabled = true
                axisMinimum = 0f
            }

            axisLeft.apply {
                isEnabled = true
                axisMinimum = 0f
            }
            axisRight.isEnabled = false

            legend.apply {
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                setDrawInside(true)
                isEnabled = true
            }
            description.isEnabled = false
            setTouchEnabled(false)

        }

        val combinedData = CombinedData()

        val distinctXAxisSet = mutableSetOf<String>()
        chartData.seriesData.forEach{
            distinctXAxisSet.addAll(it.dataList.distinctBy { it.xAxis }.mapNotNull { it.xAxis })
        }
        combinedChart.xAxis.valueFormatter = IndexAxisValueFormatter(
                chartData.xAxisValueFormatter?.formatAsList(distinctXAxisSet.toList())
                        ?: distinctXAxisSet)

        var colorCount = 0
        chartData.seriesData.forEach { it ->

            val groupByXAxis = it.dataList.groupBy { it.xAxis }
            val distinctSubgroups = it.dataList.distinctBy { it.subgroup }.mapNotNull { it.subgroup }

            if(it.series.reportSeriesVisualType == Report.BAR_CHART){

                if(distinctSubgroups.isEmpty()){

                    val barEntryList = mutableListOf<BarEntry>()
                    distinctXAxisSet.forEachIndexed { idx, xAxisKey ->
                        val barValue = groupByXAxis[xAxisKey]?.firstOrNull()?.yAxis ?: 0f
                        val barEntry = BarEntry((idx).toFloat(), chartData.yAxisValueFormatter
                                ?.asValueFormatter()?.getFormattedValue(barValue)?.toFloat() ?: barValue)
                        barEntryList.add(barEntry)
                    }

                    val barDataSet = BarDataSet(barEntryList, it.series.reportSeriesName)
                    barDataSet.setDrawValues(false)
                    barDataSet.color = Color.parseColor(colorList[colorCount++])
                    barDataSet.valueFormatter = chartData.xAxisValueFormatter?.asValueFormatter()

                    val barData = combinedData.barData?: BarData()
                    barData.barWidth = 0.9f
                    barData.addDataSet(barDataSet)
                    combinedData.setData(barData)


                }else {

                    val barData = combinedData.barData?: BarData()
                    distinctSubgroups.forEachIndexed { idx, subGroup ->

                        val barEntryList = mutableListOf<BarEntry>()
                        distinctXAxisSet.forEach { xAxisKey ->
                            val barReportData = groupByXAxis[xAxisKey]?.firstOrNull { it.subgroup == subGroup }
                            val barValue = barReportData?.yAxis ?: 0f
                            val barEntry = BarEntry((idx).toFloat(), chartData.yAxisValueFormatter
                                    ?.asValueFormatter()?.getFormattedValue(barValue)?.toFloat() ?: barValue)
                            barEntryList.add(barEntry)
                        }


                        val barDataSet = BarDataSet(barEntryList, """${it.series.reportSeriesName} 
                            - ${it.subGroupFormatter?.format(subGroup) ?: subGroup}""".trimMargin())
                        barDataSet.color = Color.parseColor(colorList[colorCount++])
                        barDataSet.setDrawValues(false)
                        barData.addDataSet(barDataSet)
                    }
                    combinedData.setData(barData)

                }


            }else if(it.series.reportSeriesVisualType == Report.LINE_GRAPH){

                if(distinctSubgroups.isEmpty()){

                    val xAxisList = mutableListOf<Entry>()
                    distinctXAxisSet.forEachIndexed { idx, xAxisKey ->
                        val barValue = groupByXAxis[xAxisKey]?.firstOrNull()?.yAxis ?: 0f
                        val barEntry = BarEntry((idx).toFloat(), chartData.yAxisValueFormatter
                                ?.asValueFormatter()?.getFormattedValue(barValue)?.toFloat() ?: barValue)
                        xAxisList.add(barEntry)
                    }

                    val lineDataSet = LineDataSet(xAxisList, it.series.reportSeriesName)
                    lineDataSet.setDrawValues(false)
                    lineDataSet.color = Color.parseColor(colorList[colorCount++])

                    val lineData = combinedData.lineData?: LineData()
                    lineData.addDataSet(lineDataSet)
                    combinedData.setData(lineData)

                }else{

                    val lineData = combinedData.lineData?: LineData()
                    distinctSubgroups.forEach { subGroup ->

                        val lineEntryList = mutableListOf<Entry>()
                        distinctXAxisSet.forEachIndexed { idx, xAxisKey ->

                            val barReportData = groupByXAxis[xAxisKey]?.firstOrNull { it.subgroup == subGroup }
                            val barValue = barReportData?.yAxis ?: 0f
                            val barEntry = Entry((idx).toFloat(), chartData.yAxisValueFormatter
                                    ?.asValueFormatter()?.getFormattedValue(barValue)?.toFloat()  ?: barValue)
                            lineEntryList.add(barEntry)
                        }

                        val barDataSet = LineDataSet(lineEntryList, """${it.series.reportSeriesName} 
                            - ${it.subGroupFormatter?.format(subGroup) ?: subGroup}""".trimMargin())
                        barDataSet.axisDependency = YAxis.AxisDependency.LEFT
                        barDataSet.color = Color.parseColor(colorList[colorCount++])
                        barDataSet.setDrawValues(false)
                        lineData.addDataSet(barDataSet)
                    }

                    combinedData.setData(lineData)

                }

            }

        }


        val barData = combinedData.barData ?: BarData()
        if(barData.dataSetCount >= 2){
            val sizeOfX = barData.dataSetCount.toFloat()
            val barSpace = 0.01f
            val groupSpace = 0.08f
            val barWidth = (1 - groupSpace) / sizeOfX - barSpace

            barData.barWidth = barWidth
            combinedChart.xAxis.mAxisMaximum = sizeOfX
            combinedChart.xAxis.mAxisMaximum = 0 +
                    combinedData.barData.getGroupWidth(groupSpace, barSpace) *
                    sizeOfX

            if (sizeOfX > 1) {
                barData.groupBars(0f, groupSpace, barSpace)
            }

            combinedChart.xAxis.setCenterAxisLabels(true)

        }


        combinedChart.data = combinedData
        combinedChart.invalidate()
        return combinedChart
    }


}