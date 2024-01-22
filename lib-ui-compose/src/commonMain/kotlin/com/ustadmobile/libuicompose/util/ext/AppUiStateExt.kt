package com.ustadmobile.libuicompose.util.ext

import com.ustadmobile.core.impl.appstate.AppUiState

fun AppUiState.copyWithNewFabOnClick(
    onClick: () -> Unit
): AppUiState {
    return copy(
        fabState = this.fabState.copy(
            onClick = onClick
        )
    )
}
