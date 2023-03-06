package com.ustadmobile.entities

import react.FC
import react.Props

data class UstadScreen<P : Props>(
    val key: String,
    val name: String,
    val component: FC<P>,
)

typealias UstadScreens = Iterable<UstadScreen<out Props>>
