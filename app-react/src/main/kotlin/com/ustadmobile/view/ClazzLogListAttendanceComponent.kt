package com.ustadmobile.view

import com.ustadmobile.core.view.ClazzLogListAttendanceView
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.GridSpacing
import com.ustadmobile.util.StyleManager.attendance
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import react.RBuilder
import styled.css
import styled.styledDiv

class ClazzLogListAttendanceComponent(mProps: UmProps) : UstadBaseComponent<UmProps, UmState>(mProps){

    override val viewName: String
        get() = ClazzLogListAttendanceView.VIEW_NAME

    override fun RBuilder.render() {
        styledDiv {
            css(attendance)
            umGridContainer(GridSpacing.spacing4) {
                umItem(GridSize.cells12){
                    renderClazzLogList{
                        console.log(it)
                    }
                }
            }
        }
    }

}