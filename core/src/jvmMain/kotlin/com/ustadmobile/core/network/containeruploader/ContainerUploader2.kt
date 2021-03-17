package com.ustadmobile.core.network.containeruploader

import com.ustadmobile.core.networkmanager.ContainerUploaderRequest
import org.kodein.di.DI
import org.kodein.di.DIAware

class ContainerUploader2(val request: ContainerUploaderRequest,
                         val chunkSize: Int = 200*1024,
                         override val di: DI) : DIAware{
}