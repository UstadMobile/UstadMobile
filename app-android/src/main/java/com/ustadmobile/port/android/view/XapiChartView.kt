package com.ustadmobile.port.android.view

//import android.content.Context
//import android.graphics.Color
//import android.util.AttributeSet
//import android.view.View
//import android.view.ViewGroup
//import android.widget.LinearLayout
//import com.github.mikephil.charting.charts.CombinedChart
//import com.github.mikephil.charting.components.Legend
//import com.github.mikephil.charting.components.XAxis
//import com.github.mikephil.charting.components.YAxis
//import com.github.mikephil.charting.data.*
//import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
//import com.ustadmobile.core.util.ext.ChartData
//import com.ustadmobile.core.util.ext.dpAspx
//import com.ustadmobile.core.util.ext.pxAsDp
//import com.ustadmobile.lib.db.entities.Report
//import com.ustadmobile.lib.db.entities.ReportSeries
//import com.ustadmobile.port.android.util.graph.asValueFormatter
//import java.lang.IllegalArgumentException
//import java.time.LocalDate
//import java.time.YearMonth
//import java.time.format.DateTimeFormatter

//Will be used/restored upon retoration of reporting/charting
//
//class XapiChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
//    : LinearLayout(context, attrs, defStyleAttr) {
//
//    var colorList = listOf("#009999", "#FF9900", "#0099FF", "#FF3333", "#663399", "#669999",
//            "#FF3366", "#990099", "#996666", "#339933", "#FFCC00", "#9966CC", "#FFCC99",
//            "#99FFCC", "#0066CC", "#66CCFF", "#FF66FF", "#4D4D4D", "#0066FF", "#FF6600", "#33FFFF",
//            "#669933","#808080", "#AF4CAB", "#0040FF","#99CC66","#B1DEFB","#FF7FAA", "#FF8000",
//            "#F0AA89", "#6AFF6A", "#339999", "#CCCCCC")
//
//    var chartView: CombinedChart? = null
//
//    fun setChartData(chartData: ChartData?) {
//        if (chartData == null) {
//            return
//        }
//        removeAllViewsInLayout()
//        chartView = createChart(chartData)
//        addView(chartView)
//    }
//
//    private fun createChart(chartData: ChartData): CombinedChart {
//
//        val combinedChart = CombinedChart(context).apply {
//            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT)
//            xAxis.apply {
//                setDrawGridLines(false)
//                isEnabled = true
//                position = XAxis.XAxisPosition.BOTTOM
//                granularity = 1f
//                labelRotationAngle = -45f
//                isGranularityEnabled = true
//                setAxisMinimum(0f)
//                this.setAvoidFirstLastClipping(true)
//            }
//            axisLeft.apply {
//                isEnabled = true
//                axisMinimum = 0f
//            }
//            axisRight.apply {
//                isEnabled = true
//                axisMinimum = 0f
//            }
//
//            legend.apply {
//                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
//                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
//                isWordWrapEnabled = true
//                setDrawInside(false)
//                isEnabled = true
//            }
//            description.isEnabled = false
//            setTouchEnabled(false)
//        }
//
//        val combinedData = CombinedData()
//        var distinctXAxisSet = chartData.seriesData.flatMap { it.dataList }
//                .mapNotNull { it.xAxis }.toSet()
//        val xAxisData = chartData.reportWithFilters.xAxis
//        if(xAxisData == Report.DAY || xAxisData == Report.WEEK){
//
//            val dateStrToLocalDate: (String) -> LocalDate = {
//                LocalDate.parse(it, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
//            }
//            distinctXAxisSet = distinctXAxisSet.sortedBy {
//                dateStrToLocalDate(it)
//            }.toSet()
//        }
//        if(xAxisData == Report.MONTH){
//
//            val dateStrToYearMonth: (String) -> YearMonth = {
//                YearMonth.parse(it, DateTimeFormatter.ofPattern("MM/yyyy"))
//            }
//            distinctXAxisSet = distinctXAxisSet.sortedBy {
//                dateStrToYearMonth(it)
//            }.toSet()
//        }
//        combinedChart.xAxis.valueFormatter = IndexAxisValueFormatter(
//                chartData.xAxisValueFormatter?.formatAsList(distinctXAxisSet.toList())
//                        ?: distinctXAxisSet)
//
//
//        var colorCount = 0
//        chartData.seriesData.forEach { it ->
//
//            val groupedByXAxis = it.dataList.filter { it.xAxis != null }.groupBy { it.xAxis }
//            val distinctSubgroups = it.dataList.mapNotNull { it.subgroup }.toSet()
//
//            if (it.series.reportSeriesVisualType == ReportSeries.BAR_CHART) {
//
//                if (distinctSubgroups.isEmpty()) {
//
//                    val barEntryList = mutableListOf<BarEntry>()
//                    distinctXAxisSet.forEachIndexed { idx, xAxisKey ->
//                        val barValue = groupedByXAxis[xAxisKey]?.firstOrNull()?.yAxis ?: 0f
//                        val barEntry = BarEntry((idx).toFloat(), chartData.yAxisValueFormatter
//                                ?.asValueFormatter()?.getFormattedValue(barValue)?.toFloat()
//                                ?: barValue)
//                        barEntryList.add(barEntry)
//                    }
//
//                    val barDataSet = BarDataSet(barEntryList, it.series.reportSeriesName)
//                    barDataSet.setDrawValues(false)
//                    try {
//                        barDataSet.color = Color.parseColor(colorList[colorCount++])
//                    }catch (e: IllegalArgumentException){
//                        e.printStackTrace()
//                    }catch (e: ArrayIndexOutOfBoundsException){
//                        colorCount = 0
//                        e.printStackTrace()
//                    }
//
//                    val barData = combinedData.barData ?: BarData()
//                    barData.addDataSet(barDataSet)
//                    combinedData.setData(barData)
//
//
//                } else {
//
//                    val barData = combinedData.barData ?: BarData()
//                    distinctSubgroups.forEach {  subGroup ->
//
//                        val barEntryList = mutableListOf<BarEntry>()
//                        distinctXAxisSet.forEachIndexed { idx, xAxisKey ->
//                            val barReportData = groupedByXAxis[xAxisKey]?.firstOrNull { it.subgroup == subGroup }
//                            val barValue = barReportData?.yAxis ?: 0f
//                            val barEntry = BarEntry((idx).toFloat(), chartData.yAxisValueFormatter
//                                    ?.asValueFormatter()?.getFormattedValue(barValue)?.toFloat()
//                                    ?: barValue)
//                            barEntryList.add(barEntry)
//                        }
//
//
//                        val barDataSet = BarDataSet(barEntryList, """${it.series.reportSeriesName} - ${it.subGroupFormatter?.format(subGroup) ?: subGroup}""".trimMargin())
//                        try {
//                            barDataSet.color = Color.parseColor(colorList[colorCount++])
//                        }catch (e: IllegalArgumentException){
//                            e.printStackTrace()
//                        }catch (e: ArrayIndexOutOfBoundsException){
//                            colorCount = 0
//                            e.printStackTrace()
//                        }
//                        barDataSet.setDrawValues(false)
//                        barData.addDataSet(barDataSet)
//                    }
//                    combinedData.setData(barData)
//
//                }
//
//
//            } else if (it.series.reportSeriesVisualType == ReportSeries.LINE_GRAPH) {
//
//                if (distinctSubgroups.isEmpty()) {
//
//                    val xAxisList = mutableListOf<Entry>()
//                    distinctXAxisSet.forEachIndexed { idx, xAxisKey ->
//                        val barValue = groupedByXAxis[xAxisKey]?.firstOrNull()?.yAxis ?: 0f
//                        val barEntry = BarEntry((idx).toFloat(), chartData.yAxisValueFormatter
//                                ?.asValueFormatter()?.getFormattedValue(barValue)?.toFloat()
//                                ?: barValue)
//                        xAxisList.add(barEntry)
//                    }
//
//                    val lineDataSet = LineDataSet(xAxisList, it.series.reportSeriesName)
//                    lineDataSet.setDrawValues(false)
//                    try {
//                        lineDataSet.color = Color.parseColor(colorList[colorCount++])
//                    }catch (e: IllegalArgumentException){
//                        e.printStackTrace()
//                    }catch (e: ArrayIndexOutOfBoundsException){
//                        colorCount = 0
//                        e.printStackTrace()
//                    }
//
//                    val lineData = combinedData.lineData ?: LineData()
//                    lineData.addDataSet(lineDataSet)
//                    combinedData.setData(lineData)
//
//                } else {
//
//                    val lineData = combinedData.lineData ?: LineData()
//                    distinctSubgroups.forEach { subGroup ->
//
//                        val lineEntryList = mutableListOf<Entry>()
//                        distinctXAxisSet.forEachIndexed { idx, xAxisKey ->
//
//                            val barReportData = groupedByXAxis[xAxisKey]?.firstOrNull { it.subgroup == subGroup }
//                            val barValue = barReportData?.yAxis ?: 0f
//                            val barEntry = Entry((idx).toFloat(), chartData.yAxisValueFormatter
//                                    ?.asValueFormatter()?.getFormattedValue(barValue)?.toFloat()
//                                    ?: barValue)
//                            lineEntryList.add(barEntry)
//                        }
//
//                        val lineDataSet = LineDataSet(lineEntryList, """${it.series.reportSeriesName} - ${it.subGroupFormatter?.format(subGroup) ?: subGroup}""".trimMargin()).apply {
//                            axisDependency = YAxis.AxisDependency.LEFT
//                            try {
//                                val colorSelected = Color.parseColor(colorList[colorCount++])
//                                color = colorSelected
//                                setCircleColor(colorSelected)
//                            }catch (e: IllegalArgumentException){
//                                e.printStackTrace()
//                            }catch (e: ArrayIndexOutOfBoundsException){
//                                colorCount = 0
//                                e.printStackTrace()
//                            }
//                            setDrawValues(false)
//                            lineWidth = 2.5f
//
//                        }
//                        lineData.addDataSet(lineDataSet)
//                    }
//
//                    combinedData.setData(lineData)
//
//                }
//
//            }
//
//        }
//
//
//        val barData = combinedData.barData
//        /**
//         *  if bar data has multiple series or the series has a subgroup
//         *  then redraw the bars using barData.groupBars to fit all the bars into 1 chart
//         */
//        if (barData != null && barData.dataSetCount > 1) {
//            val numberOfDataSets = barData.dataSetCount.toFloat()
//            val barSpace = 0.01f
//            val groupSpace = 0.05f
//            val barWidth = ((1 - groupSpace) / numberOfDataSets) - barSpace
//
//            barData.barWidth = barWidth
//            combinedChart.xAxis.setCenterAxisLabels(true)
//            // do not change this for group
//            combinedChart.xAxis.setAxisMaximum(distinctXAxisSet.size.toFloat())
//            barData.groupBars(0f, groupSpace, barSpace)
//
//        }
//
//
//        val yAxisMax = combinedData.yMax * 0.25f
//        combinedChart.axisLeft.apply {
//            axisMaximum = combinedData.yMax + yAxisMax
//        }
//        combinedChart.axisRight.apply {
//            axisMaximum = combinedData.yMax + yAxisMax
//        }
//
//
//
//        if(combinedData.barData != null || combinedData.lineData != null){
//            combinedChart.data = combinedData
//            val height = (250.dpAspx + (colorCount * 15).dpAspx).toInt()
//            val linearParams = super.getLayoutParams()
//            linearParams.height = height
//            super.setLayoutParams(linearParams)
//            super.invalidate()
//        }
//
//        combinedChart.invalidate()
//        return combinedChart
//    }
//
//
//}