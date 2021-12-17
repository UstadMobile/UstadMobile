package com.ustadmobile.core.network.containeruploader

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.network.NetworkProgressListener
import org.kodein.di.DI

actual class ContainerUploader2 actual constructor(
    request: ContainerUploaderRequest2,
    chunkSize: Int,
    endpoint: Endpoint,
    progressListener: NetworkProgressListener?,
    di: DI
) {
    actual suspend fun upload(): Int {
        TODO("Not yet implemented")
    }


}