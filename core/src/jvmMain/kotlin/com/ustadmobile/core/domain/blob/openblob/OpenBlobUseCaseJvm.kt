package com.ustadmobile.core.domain.blob.openblob

import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File

class OpenBlobUseCaseJvm(
    private val getStoragePathForUrlUseCase: GetStoragePathForUrlUseCase,
    private val rootTmpDir: File
) : OpenBlobUseCase {

    override suspend fun invoke(
        item: OpenBlobItem,
        onProgress: (bytesTransferred: Long, totalBytes: Long) ->  Unit,
    ) = withContext(Dispatchers.IO){
        val storageFileUri = getStoragePathForUrlUseCase(
            url = item.uri,
            onStateChange = {
                onProgress(it.bytesTransferred, it.totalBytes)
            }
        )

        //Do not directly open the file from the cache dir - if opened using an editing program,
        //this would corrupt the cache
        val openFromDir = File(rootTmpDir, "open-${systemTimeInMillis()}")
        openFromDir.mkdirs()
        val openFromFile = File(openFromDir, item.fileName)
        val storageFile = DoorUri.parse(storageFileUri).toFile()
        storageFile.copyTo(openFromFile)
        Desktop.getDesktop().open(openFromFile)
    }
}