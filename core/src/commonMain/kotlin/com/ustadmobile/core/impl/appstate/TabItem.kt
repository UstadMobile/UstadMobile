package com.ustadmobile.core.impl.appstate

data class TabItem(
    val viewName: String,
    val args: Map<String, String>,
    val label: String,
) {
}