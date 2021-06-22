package com.ustadmobile.navigation

import react.Component
import react.RProps
import kotlin.reflect.KClass

data class UstadDestination(var icon: String?= null,
                            var labelId: Int = 0,
                            var view: String,
                            var component: KClass<out Component<RProps, *>>,
                            var showSearch: Boolean = false,
                            var showNavigation: Boolean = true,
                            var divider: Boolean = false)