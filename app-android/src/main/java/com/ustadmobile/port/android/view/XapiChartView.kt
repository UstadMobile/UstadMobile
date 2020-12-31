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

    var colorList = listOf("#009688", "#FF9800", "#2196F3", "#f44336", "#673AB7", "#607D8B", "#E91E63", "#9C27B0", "#795548", "#4CAF50")

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
            axisRight.apply {
                isEnabled = true
                axisMinimum = 0f
            }

            legend.apply {
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                isWordWrapEnabled = true
                setDrawInside(true)
                isEnabled = true
            }
            description.isEnabled = false
            setTouchEnabled(false)

        }

        val combinedData = CombinedData()

        val distinctXAxisSet = chartData.seriesData.flatMap { it.dataList }
                .mapNotNull { it.xAxis }.toSet()
        // TODO consider sorting for calendar
        combinedChart.xAxis.valueFormatter = IndexAxisValueFormatter(
                chartData.xAxisValueFormatter?.formatAsList(distinctXAxisSet.toList())
                        ?: distinctXAxisSet)
        combinedChart.xAxis.labelCount = distinctXAxisSet.size


        var colorCount = 0
        chartData.seriesData.forEach { it ->

            val groupedByXAxis = it.dataList.groupBy { it.xAxis }
            val distinctSubgroups = it.dataList.mapNotNull { it.subgroup }.toSet()

            if(it.series.reportSeriesVisualType == Report.BAR_CHART){

                if(distinctSubgroups.isEmpty()){

                    val barEntryList = mutableListOf<BarEntry>()
                    distinctXAxisSet.forEachIndexed { idx, xAxisKey ->
                        val barValue = groupedByXAxis[xAxisKey]?.firstOrNull()?.yAxis ?: 0f
                        val barEntry = BarEntry((idx).toFloat(), chartData.yAxisValueFormatter
                                ?.asValueFormatter()?.getFormattedValue(barValue)?.toFloat() ?: barValue)
                        barEntryList.add(barEntry)
                    }

                    val barDataSet = BarDataSet(barEntryList, it.series.reportSeriesName)
                    barDataSet.setDrawValues(false)
                    barDataSet.color = Color.parseColor(colorList[colorCount++])

                    val barData = combinedData.barData?: BarData()
                    barData.addDataSet(barDataSet)
                    combinedData.setData(barData)


                }else {

                    val barData = combinedData.barData?: BarData()
                    distinctSubgroups.forEachIndexed { idx, subGroup ->

                        val barEntryList = mutableListOf<BarEntry>()
                        distinctXAxisSet.forEach { xAxisKey ->
                            val barReportData = groupedByXAxis[xAxisKey]?.firstOrNull { it.subgroup == subGroup }
                            val barValue = barReportData?.yAxis ?: 0f
                            val barEntry = BarEntry((idx).toFloat(), chartData.yAxisValueFormatter
                                    ?.asValueFormatter()?.getFormattedValue(barValue)?.toFloat() ?: barValue)
                            barEntryList.add(barEntry)
                        }


                        val barDataSet = BarDataSet(barEntryList, """${it.series.reportSeriesName} - ${it.subGroupFormatter?.format(subGroup) ?: subGroup}""".trimMargin())
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
                        val barValue = groupedByXAxis[xAxisKey]?.firstOrNull()?.yAxis ?: 0f
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

                            val barReportData = groupedByXAxis[xAxisKey]?.firstOrNull { it.subgroup == subGroup }
                            val barValue = barReportData?.yAxis ?: 0f
                            val barEntry = Entry((idx).toFloat() + 0.5f, chartData.yAxisValueFormatter
                                    ?.asValueFormatter()?.getFormattedValue(barValue)?.toFloat()  ?: barValue)
                            lineEntryList.add(barEntry)
                        }

                        val lineDataSet = LineDataSet(lineEntryList, """${it.series.reportSeriesName} - ${it.subGroupFormatter?.format(subGroup) ?: subGroup}""".trimMargin()).apply {
                            axisDependency = YAxis.AxisDependency.LEFT
                            val colorSelected = Color.parseColor(colorList[colorCount++])
                            color = colorSelected
                            setCircleColor(colorSelected)
                            setDrawValues(false)
                            lineWidth = 2.5f

                        }
                        lineData.addDataSet(lineDataSet)
                    }

                    combinedData.setData(lineData)

                }

            }

        }


        val barData = combinedData.barData ?: BarData()
        if(barData.dataSetCount >= 2){
            val numberOfDataSets = barData.dataSetCount.toFloat()
            val barSpace = 0.01f
            val groupSpace = 0.08f
            val barWidth = (1 - groupSpace) / numberOfDataSets - barSpace

            barData.barWidth = barWidth
            combinedChart.xAxis.mAxisMaximum = numberOfDataSets
            combinedChart.xAxis.mAxisMaximum = combinedData.barData
                    .getGroupWidth(groupSpace, barSpace) * numberOfDataSets
            if (barData.dataSetCount  > 1) {
                barData.groupBars(0f, groupSpace, barSpace)
            }

        }



        combinedChart.data = combinedData
        combinedChart.invalidate()
        return combinedChart
    }


}