package com.ustadmobile.view

import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.GridSpacing
import com.ustadmobile.mui.umChart
import com.ustadmobile.util.StyleManager.attendance
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

class ClazzLogListAttendanceComponent(mProps: UmProps) : UstadBaseComponent<UmProps, UmState>(mProps){

    override val viewName: String
        get() = ""

    var chartData: Array<Array<Any>>? = null;

    override fun RBuilder.render() {
        styledDiv {
            css(attendance)
            umGridContainer(columnSpacing = GridSpacing.spacing4) {
                umItem(GridSize.cells12, GridSize.cells7){
                    chartData?.let {
                        umChart(it){}
                    }
                }

                umItem(GridSize.cells12, GridSize.cells5){
                    renderClazzLogList{ data ->
                        console.log(data)
                        setState {

                        }
                    }
                }
            }
        }
    }

}