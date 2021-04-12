package com.ustadmobile.model

import react.Component
import react.RProps
import kotlin.reflect.KClass

data class UmReactDestination(var icon: String?= null, var labelId: Int = 0,
                              var view: String, var component: KClass<out Component<RProps, *>>,
                              var showSearch: Boolean = false, var divider: Boolean = false)
