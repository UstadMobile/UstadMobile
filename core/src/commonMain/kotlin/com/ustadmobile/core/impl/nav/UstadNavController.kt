package com.ustadmobile.core.impl.nav

import com.ustadmobile.core.impl.UstadMobileSystemCommon

/**
 * Interface to form the basis of a multiplatform wrapper for Jetpack's NavController. To handle
 * SavedState, navigation.
 *
 * Structure is intended to be similar to Android JetPack navigation. On Android this is implemented
 * using an Adapter over the real NavController. On Javascript this will use window.location, react
 * router, and its own code for savedstate and history tracking.
 *
 * ViewName refers to the VIEWNAME on the View interface. ViewName is used the same as a destination
 * id on Android.
 */
interface UstadNavController {

    val currentBackStackEntry: UstadBackStackEntry?

    fun getBackStackEntry(viewName: String): UstadBackStackEntry?

    fun popBackStack(viewName: String, inclusive: Boolean)

    fun navigate(viewName: String, args: Map<String, String>,
                 goOptions: UstadMobileSystemCommon.UstadGoOptions = UstadMobileSystemCommon.UstadGoOptions.Default)
}