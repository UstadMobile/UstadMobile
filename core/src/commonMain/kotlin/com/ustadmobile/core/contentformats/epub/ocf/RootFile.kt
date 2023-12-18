package com.ustadmobile.core.contentformats.epub.ocf

import com.ustadmobile.core.contentformats.epub.ocf.Container.Companion.NS_CONTAINER
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName(
    value = "rootfile",
    namespace = NS_CONTAINER,
)
class RootFile(
    @SerialName("full-path")
    val fullPath: String,
    @SerialName("media-type")
    val mediaType: String,
) {
}