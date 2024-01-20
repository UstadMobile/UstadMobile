package com.ustadmobile.core.domain.cachestoragepath

import com.ustadmobile.core.io.ext.isRemote
import com.ustadmobile.door.DoorUri

suspend fun GetStoragePathForUrlUseCase.getLocalUriIfRemote(
    uri: DoorUri
) : DoorUri {
    return if(uri.isRemote()) {
        DoorUri.parse(invoke(url = uri.toString()))
    }else {
        uri
    }
}