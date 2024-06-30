package com.ustadmobile.core.util

import kotlinx.coroutines.flow.StateFlow

interface FolderSelector {
    val selectedFolder: StateFlow<String?>
    suspend fun selectFolder()
}

expect fun createFolderSelector(context: Any): FolderSelector