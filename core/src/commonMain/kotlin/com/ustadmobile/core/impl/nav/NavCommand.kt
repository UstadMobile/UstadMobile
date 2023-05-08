package com.ustadmobile.core.impl.nav

import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.door.util.systemTimeInMillis

/**
 * @param timestamp - this can be used to identify nav commands and record if they have been
 * processed.
 */
sealed class NavCommand(val timestamp: Long)

data class NavigateNavCommand(
    val viewName: String,
    val args: Map<String, String>,
    val goOptions: UstadMobileSystemCommon.UstadGoOptions,
): NavCommand(systemTimeInMillis())

data class PopNavCommand(
    val viewName: String,
    val inclusive: Boolean
): NavCommand(systemTimeInMillis())

class TestNavCommand(timestamp: Long): NavCommand(timestamp)

