package com.ustadmobile.core.io

import com.ustadmobile.door.DoorUri

class ContainerUriSource(
    val pathInContainer: String,
    val uri: DoorUri,
    val context: Any,
    val compression: ContainerBuilder.Compression = ContainerBuilder.Compression.GZIP,
): ContainerBuilder.ContainerSource() {
}