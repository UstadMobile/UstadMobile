package com.ustadmobile.view

import com.ustadmobile.util.RouteManager
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import styled.styledDiv

class PlaceHolderComponent(props: RProps): RComponent<RProps,RState>(props) {
    override fun RBuilder.render() {
        styledDiv {
            +"${RouteManager.getPathName()}"
        }
    }
}