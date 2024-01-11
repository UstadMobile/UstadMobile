package com.ustadmobile.libuicompose.components

typealias LaunchFilePickFn = (UstadPickFileOpts) -> Unit

data class UstadPickFileOpts(
    val mimeFilters: List<String> = emptyList()
)

data class UstadFilePickResult(
    val uri: String,
    val fileName: String,
)

