package com.ustadmobile.core.contentformats.epub.ocf

import com.ustadmobile.core.contentformats.epub.ocf.Container.Companion.NS_CONTAINER
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName(
    value = "rootfiles",
    namespace = NS_CONTAINER
)
class RootFiles(
    val rootFiles: List<RootFile>,
) {
}