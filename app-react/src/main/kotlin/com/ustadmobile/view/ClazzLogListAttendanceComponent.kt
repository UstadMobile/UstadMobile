package com.ustadmobile.view

import com.ustadmobile.core.view.ClazzLogListAttendanceView
import react.RBuilder
import com.ustadmobile.util.*

class ClazzLogListAttendanceComponent(mProps: UmProps) : UstadBaseComponent<UmProps, UmState>(mProps){

    override val viewName: String
        get() = ClazzLogListAttendanceView.VIEW_NAME


    override fun RBuilder.render() {
       /* styledDiv {
            css(attendance)
            umGridContainer(MGridSpacing.spacing4) {

                umItem(MGridSize.cells12, MGridSize.cells7){

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
        }*/
    }

}