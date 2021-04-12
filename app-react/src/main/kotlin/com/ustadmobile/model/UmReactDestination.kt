package com.ustadmobile.model

import react.Component
import react.RProps
import kotlin.reflect.KClass

/**
 * Represent a destination point of an app
 */
data class UmReactDestination(var icon: String?= null, var labelId: Int = 0,
                              var view: String, var component: KClass<out Component<RProps, *>>,
                              var showSearch: Boolean = false, var divider: Boolean = false,
                              var args: Map<String,String> = mapOf())
