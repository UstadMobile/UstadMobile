package com.ustadmobile.libuicompose.components

enum class PickType { FILE, FOLDER }

data class PickFileOptions(
    val fileExtensions: List<String> = emptyList(),
    val mimeTypes: List<String> = emptyList(),
    val pickType: PickType = PickType.FILE
)

typealias LaunchFilePickFn = (PickFileOptions) -> Unit

data class UstadFilePickResult(
    val uri: String,
    val fileName: String,
    val mimeType: String?,
    val size: Long,
)

// Keep this for backward compatibility
data class UstadPickFileOpts(
    val mimeFilters: List<String> = emptyList()
)