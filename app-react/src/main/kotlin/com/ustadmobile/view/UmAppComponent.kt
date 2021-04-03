package com.ustadmobile.view

import com.ustadmobile.state.UmBaseState
import react.RBuilder
import react.RProps
import styled.styledDiv

class UmAppComponent (props: RProps): UmBaseComponent<RProps, UmBaseState>(props) {
    override fun RBuilder.render() {
        styledDiv {
            +"Hello"
        }
    }
}

fun RBuilder.umBaseApp() = child(UmAppComponent::class) {}