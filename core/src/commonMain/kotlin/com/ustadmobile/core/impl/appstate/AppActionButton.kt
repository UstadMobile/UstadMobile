package com.ustadmobile.core.impl.appstate

data class AppActionButton(
    val icon: AppStateIcon,
    val contentDescription: String,
    val onClick: () -> Unit,
)
