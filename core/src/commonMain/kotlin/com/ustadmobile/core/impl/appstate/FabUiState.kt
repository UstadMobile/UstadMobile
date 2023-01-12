package com.ustadmobile.core.impl.appstate

/**
 * Represents the Floating Action Button.
 */
data class FabUiState(
    val visible: Boolean = false,
    val text: String? = null,
    val onClick: () -> Unit = { },

) {
}