package com.ustadmobile.core.io.ext

import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
import org.kodein.di.DI
import java.io.File

actual suspend fun ProcessContext.getLocalUri(fileUri: DoorUri, context: Any, di: DI): DoorUri{

    if(fileUri.isRemote()){

        // if cached return it
        if(localUri != null){
            return localUri as DoorUri
        }

        // download it
        val destFile = File(tempDirUri.toFile(), fileUri.getFileName(context))
        val destUri = DoorUri.parse(destFile.toURI().toString())
        fileUri.downloadUrl(tempDirUri, destUri, di){

        }

        // cache it
        localUri = destUri

        return destUri

    }else{
        return fileUri
    }

}