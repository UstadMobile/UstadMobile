package com.ustadmobile.core.io

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ContainerZipSource(
    val zipInput: () -> ZipInputStream,
    val pathInContainerPrefix: String,
    val compression: (ZipEntry) -> ContainerBuilder.Compression,
) : ContainerBuilder.ContainerSource(){


}