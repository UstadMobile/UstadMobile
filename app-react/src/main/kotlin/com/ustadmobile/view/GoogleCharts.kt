package com.ustadmobile.mui

import com.ustadmobile.mui.ext.createStyledComponent
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

external interface GoogleChartsProps: StyledProps {
    var width: String
    var height: String
    var chartType: String
    var data: Array<Array<Any>>
    var options: Any?
}

enum class MChartType(val chartType: String){
    AreaChart("AreaChart"),
    ColumnChart("ColumnChart"),
    LineChart("LineChart")
}

fun RBuilder.umChart(data: Array<Array<Any>>,
                    width: Any? = "100%",
                    height: Any? = "400px",
                    chartType: MChartType = MChartType.ColumnChart,
                    options: Any? = null,
                    className: String? = null,
                    handler: StyledHandler<GoogleChartsProps>
) = createStyledComponent(googleChartsComponent.unsafeCast<ComponentType<GoogleChartsProps>>(), className, handler) {
    attrs.width = width.toString()
    attrs.height = height.toString()
    attrs.chartType = chartType.toString()
    attrs.data = data
    attrs.options = options
}
