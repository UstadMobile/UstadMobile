package com.ustadmobile.core.impl.nav

/**
 * Interface to form the basis of a multiplatform wrapper for Jetpack's NavController. To handle
 * SavedState, navigation.
 */
interface UstadNavController {

    val currentBackStackEntry: UstadBackStackEntry?

    fun getBackStackEntry(viewName: String): UstadBackStackEntry?

}