package com.ustadmobile.view.components

import mui.material.Typography
import react.FC
import react.PropsWithChildren

external interface UstadEditHeaderProps: PropsWithChildren

val UstadEditHeader = FC<UstadEditHeaderProps> { props ->
    Typography {
        +props.children
    }
}
