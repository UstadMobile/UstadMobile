package com.ustadmobile.view

import com.ustadmobile.mui.ext.convertFunctionalToClassElement
import com.ustadmobile.util.UmState
import react.ComponentType
import react.RBuilder
import react.RComponent
import styled.StyledHandler
import styled.StyledProps

@JsModule("react-google-charts")
@JsNonModule
private external val googleChartsModule: dynamic

@Suppress("UnsafeCastFromDynamic")
private val googleChartsComponent: RComponent<GoogleChartsProps, UmState> = googleChartsModule.default

external interface ChartAxis{
    var title: String?
}

data class  ChartOptions(
    var title: String? = null,
    var hAxis: ChartAxis? = null,
    var xAxis: ChartAxis? = null,
    var seriesType: String? = null,
    var series: Any? = null,
    var colors: Array<String>? = null
)

external interface GoogleChartsProps: StyledProps {
    var width: String
    var height: String
    var chartType: String
    var data: Array<Array<Any>>
    var options: ChartOptions?
}

enum class ChartType{
    ColumnChart,
    ComboChart,
    LineChart
}

fun RBuilder.umChart(
    data: Array<Array<Any>>,
    width: Any? = "100%",
    height: Any? = "400px",
    chartType: ChartType = ChartType.ColumnChart,
    options: ChartOptions? = null,
    className: String? = null,
    handler: StyledHandler<GoogleChartsProps>? = null
) = convertFunctionalToClassElement(googleChartsComponent.unsafeCast<ComponentType<GoogleChartsProps>>(), className, handler) {
    attrs.width = width.toString()
    attrs.height = height.toString()
    attrs.chartType = chartType.toString()
    attrs.data = data
    attrs.options = options
}
