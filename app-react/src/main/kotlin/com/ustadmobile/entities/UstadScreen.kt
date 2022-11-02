package com.ustadmobile.entities

import react.FC
import react.Props

data class UstadScreen(
    val key: String,
    val name: String,
    val Component: FC<Props>,
)

typealias UstadScreens = Iterable<UstadScreen>
