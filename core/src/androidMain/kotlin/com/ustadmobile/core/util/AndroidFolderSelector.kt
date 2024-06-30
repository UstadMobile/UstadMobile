package com.ustadmobile.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow

class AndroidFolderSelector(private val context: Context) : FolderSelector {
    override val selectedFolder = MutableStateFlow<String?>(null)

    private var folderPickerCallback: ((Intent) -> Unit)? = null

    override suspend fun selectFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        folderPickerCallback?.invoke(intent)
    }

    fun setFolderPickerCallback(callback: (Intent) -> Unit) {
        folderPickerCallback = callback
    }

    fun onFolderSelected(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        selectedFolder.value = uri.toString()
    }
}

actual fun createFolderSelector(context: Any): FolderSelector = AndroidFolderSelector(context as Context)
