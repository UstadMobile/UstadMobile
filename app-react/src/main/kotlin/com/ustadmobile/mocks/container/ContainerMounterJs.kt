package com.ustadmobile.mocks.container

import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContainerMounter

class ReactContainerMounter: ContainerMounter {
    override suspend fun mountContainer(endpointUrl: String, containerUid: Long, filterMode: Int): String {
        return UMFileUtil.joinPaths(endpointUrl,"",containerUid.toString())
    }

    override suspend fun unMountContainer(endpointUrl: String, mountPath: String) {
        TODO("Not yet implemented")
    }
}