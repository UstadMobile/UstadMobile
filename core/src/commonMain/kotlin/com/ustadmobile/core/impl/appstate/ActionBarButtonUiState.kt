package com.ustadmobile.core.impl.appstate

/**
 * Represents the state of the action bar button e.g. the Save/Done button in the top right.
 */
data class ActionBarButtonUiState(
    val visible: Boolean = false,
    val text: String? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit = { },
)
