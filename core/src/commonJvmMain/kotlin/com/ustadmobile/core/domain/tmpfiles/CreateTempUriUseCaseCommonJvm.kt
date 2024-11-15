package com.ustadmobile.core.domain.tmpfiles

import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.util.systemTimeInMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CreateTempUriUseCaseCommonJvm(
    private val rootTmpDir: File,
): CreateTempUriUseCase {

    override suspend fun invoke(prefix: String, postfix: String): DoorUri = withContext(Dispatchers.IO){
        File(rootTmpDir, "$prefix-tmp-${systemTimeInMillis()}$postfix").apply {
            parentFile?.takeIf { !it.exists() }?.mkdirs()
            createNewFile()
        }.toDoorUri()
    }
}