package com.ustadmobile.core.impl.nav

import com.ustadmobile.core.impl.UstadMobileSystemCommon

sealed class NavCommand

data class NavigateNavCommand(
    val viewName: String,
    val args: Map<String, String>,
    val goOptions: UstadMobileSystemCommon.UstadGoOptions,
): NavCommand()

data class PopNavCommand(
    val viewName: String,
    val inclusive: Boolean
): NavCommand()
