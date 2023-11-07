package com.ustadmobile.core.contentformats.epub.ocf

import com.ustadmobile.core.contentformats.epub.ocf.Container.Companion.NS_CONTAINER
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName


@Serializable
@XmlSerialName(
    value = "container",
    namespace = NS_CONTAINER,
)
class Container(
    val rootFiles: RootFiles? = null,
    val version: String? = null,
) {

    companion object {

        const val NS_CONTAINER = "urn:oasis:names:tc:opendocument:xmlns:container"

    }

}