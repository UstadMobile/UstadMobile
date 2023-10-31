package com.ustadmobile.core.contentformats.epub.opf

import com.ustadmobile.core.contentformats.epub.opf.Package.Companion.NS_OPF
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@XmlSerialName(
    value = "package",
    namespace = NS_OPF,
)
@Serializable
class Package(
    val version: String? = null,

    //Required as per the OPF spec
    @XmlSerialName(value = "unique-identifier")
    val uniqueIdentifier: String = "",

    //Required as per the OPF spec https://www.w3.org/submissions/2017/SUBM-epub-packages-20170125/#sec-metadata-elem
    val metadata: Metadata = Metadata(),

    val manifest: Manifest = Manifest(),

    val spine: Spine = Spine(),
) {

    /**
     * As per the OPF spec, the package element has an idref to a DC:identifier element in the
     * metadata.
     */
    fun uniqueIdentifierContent(): String? {
        return metadata.identifiers.firstOrNull { it.id == uniqueIdentifier }?.content
    }

    companion object {

        const val NS_OPF = "http://www.idpf.org/2007/opf"

        const val NS_DC = "http://purl.org/dc/elements/1.1/"
    }
}