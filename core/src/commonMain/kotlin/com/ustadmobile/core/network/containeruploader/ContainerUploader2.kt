package com.ustadmobile.core.network.containeruploader

import com.ustadmobile.core.account.Endpoint
import org.kodein.di.DI

expect class ContainerUploader2(
    request: ContainerUploaderRequest2,
    chunkSize: Int = 200 * 1024 /*200K*/,
    endpoint: Endpoint,
    di: DI
) {

    suspend fun upload(): Int


}