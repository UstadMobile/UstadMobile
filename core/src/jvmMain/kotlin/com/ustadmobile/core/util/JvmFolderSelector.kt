package com.ustadmobile.core.util


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

class JvmFolderSelector : FolderSelector {
    override val selectedFolder = MutableStateFlow<String?>(null)

    override suspend fun selectFolder() {
        withContext(Dispatchers.IO) {
            SwingUtilities.invokeAndWait {
                val fileChooser = JFileChooser().apply {
                    fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                    dialogTitle = "Select a folder"
                }
                val result = fileChooser.showOpenDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFolder.value = fileChooser.selectedFile.absolutePath
                }
            }
        }
    }
}

actual fun createFolderSelector(context: Any): FolderSelector = JvmFolderSelector()