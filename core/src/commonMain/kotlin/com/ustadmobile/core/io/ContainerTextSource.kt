package com.ustadmobile.core.io

class ContainerTextSource(
    val pathInContainer: String,
    val text: String,
    val compression: ContainerBuilder.Compression,
) : ContainerBuilder.ContainerSource()

