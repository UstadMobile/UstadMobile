package com.ustadmobile.core.io

import java.util.zip.ZipInputStream

class ContainerZipSource(
    val zipInput: () -> ZipInputStream,
    val pathInContainerPrefix: String,
    val compression: PathCompressionFilter = DefaultPathCompressionFilter(),
) : ContainerBuilder.ContainerSource(){


}