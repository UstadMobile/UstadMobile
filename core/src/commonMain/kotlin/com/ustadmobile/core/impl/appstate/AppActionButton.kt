package com.ustadmobile.core.impl.appstate

/**
 * @param id Used to set the DOM id for the action button on React. Sets the testTag on Compose.
 */
data class AppActionButton(
    val icon: AppStateIcon,
    val contentDescription: String,
    val onClick: () -> Unit,
    val id: String,
)
