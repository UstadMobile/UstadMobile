package com.ustadmobile.core.domain.blob.openblob

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class OpenBlobUseCaseAndroid(
    private val appContext: Context,
    private val getStoragePathForUrlUseCase: GetStoragePathForUrlUseCase,
) : OpenBlobUseCase{

    override suspend fun invoke(
        item: OpenBlobItem,
        onProgress: (bytesTransferred: Long, totalBytes: Long) -> Unit,
        intent: OpenBlobUseCase.OpenBlobIntent,
    ) {
        val storageFileUri = getStoragePathForUrlUseCase(
            url = item.uri,
            onStateChange = {
                onProgress(it.bytesTransferred, it.totalBytes)
            }
        )
        val storageFile = DoorUri.parse(storageFileUri).toFile()

        val fromFile = if(intent == OpenBlobUseCase.OpenBlobIntent.SEND) {
            //Unfortunately the share seems to ignore the file name parameter, so we need to copy it
            //to the cache dir
            withContext(Dispatchers.IO) {
                val openCacheDir = File(appContext.cacheDir, "open")
                openCacheDir.takeIf { !it.exists() }?.mkdirs()
                val cacheFile = File(openCacheDir, item.fileName)
                storageFile.copyTo(cacheFile, overwrite = true)
                cacheFile
            }
        }else {
            storageFile
        }

        val androidUriForIntent = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.provider",
            fromFile,
            item.fileName
        )

        val openIntent = if(intent == OpenBlobUseCase.OpenBlobIntent.VIEW) {
            Intent().apply {
                action = Intent.ACTION_VIEW
                setDataAndType(androidUriForIntent, item.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }else {
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, androidUriForIntent)
                type = item.mimeType
            }
        }

        val chooser = Intent.createChooser(openIntent, null)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(chooser)
    }
}