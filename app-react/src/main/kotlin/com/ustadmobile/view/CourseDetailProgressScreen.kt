package com.ustadmobile.view

import com.ustadmobile.core.hooks.useStringsXml
import csstype.px
import mui.material.*
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props

external interface CourseDetailProgressProps : Props {

}

val CourseDetailProgressScreenPreview = FC<Props> {
    CourseDetailProgressScreenComponent2 {

    }
}

val CourseDetailProgressScreenComponent2 = FC<CourseDetailProgressProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

        }
    }
}