package com.ustadmobile.navigation

import com.ustadmobile.util.UmProps
import react.Component
import kotlin.reflect.KClass

data class UstadDestination(
    var icon: String?= null,
    var labelId: Int = 0,
    var view: String,
    var component: KClass<out Component<UmProps, *>>,
    var showSearch: Boolean = false,
    var showNavigation: Boolean = true,
    var divider: Boolean = false
)