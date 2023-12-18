package com.ustadmobile.core.impl.nav

import com.ustadmobile.core.impl.UstadMobileSystemCommon

/**
 * Interface for multiplatform navigation. On Compose platforms this is implemented using PreCompose
 * (very similar to Jetpack Compose navigation).
 *
 * On React/JS the React router is used.
 *
 * ViewName refers to the destination name (e.g. route) on the ViewModel.
 */
interface UstadNavController {

    fun popBackStack(viewName: String, inclusive: Boolean)

    fun navigate(
        viewName: String,
        args: Map<String, String>,
        goOptions: UstadMobileSystemCommon.UstadGoOptions = UstadMobileSystemCommon.UstadGoOptions.Default
    )
}
