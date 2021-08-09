package com.ustadmobile.view

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.mButton
import com.ustadmobile.core.view.ClazzLogListAttendanceView
import com.ustadmobile.util.StyleManager.attendance
import com.ustadmobile.view.components.MChartType
import com.ustadmobile.view.components.mChart
import com.ustadmobile.view.components.mDatePicker
import com.ustadmobile.view.components.mTimePicker
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import kotlinx.css.margin
import react.RBuilder
import react.RProps
import react.RState
import styled.css
import styled.styledDiv
import kotlin.js.Date

class ClazzLogListAttendanceComponent(mProps: RProps) : UstadBaseComponent<RProps, RState>(mProps){

    override val viewName: String
        get() = ClazzLogListAttendanceView.VIEW_NAME


    override fun RBuilder.render() {
        styledDiv {
            css(attendance)
            umGridContainer(MGridSpacing.spacing4) {

                umItem(MGridSize.cells12, MGridSize.cells7){
                    mDatePicker("MM/DD/YYYY"){
                        attrs.render = { value,open ->
                            mTextField("Date", value.toString()){
                                attrs.asDynamic().onClick = { open.invoke()}
                            }
                        }
                    }

                    mChart(arrayOf(
                        arrayOf("Year", "Sales", "Expenses"),
                        arrayOf("2013", 1000, 400),
                        arrayOf("2014", 1170, 460),
                        arrayOf("2015", 660, 1120)),
                        chartType = MChartType.ColumnChart,
                        width = "100%",
                        height = "500px"){
                        css{
                            margin(3.spacingUnits)
                        }
                    }
                }

                umItem(MGridSize.cells12, MGridSize.cells5){
                    renderClazzLogList{
                        console.log(it)
                    }
                }
            }
        }
    }

}