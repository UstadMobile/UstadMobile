package com.ustadmobile.core.io

import java.io.File

class ContainerFileSource(
    val pathInContainer: String,
    val file: File,
    val compression: ContainerBuilder.Compression,
    val moveOriginalFile: Boolean = false,
) : ContainerBuilder.ContainerSource() {
}