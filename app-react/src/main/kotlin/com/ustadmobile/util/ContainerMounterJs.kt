package com.ustadmobile.util

import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContainerMounter

class ContainerMounterJs: ContainerMounter {
    override suspend fun mountContainer(endpointUrl: String, containerUid: Long, filterMode: Int): String {
        return UMFileUtil.joinPaths(endpointUrl,"ContainerMount",containerUid.toString())
    }

    override suspend fun unMountContainer(endpointUrl: String, mountPath: String) {
        console.log("Unmount container ", mountPath)
    }
}