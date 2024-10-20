package com.ustadmobile.core.contentformats.epub.opf

import com.ustadmobile.core.contentformats.epub.opf.PackageDocument.Companion.NS_OPF
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@XmlSerialName(
    value = "package",
    namespace = NS_OPF,
)
@Serializable
class PackageDocument(
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

    /**
     * Get the manifest item representing the cover image url, if any
     */
    fun coverItem() : Item? {
        val whitespaceRegex = Regex("\\s+")

        /**
         * First try the EPUB3 way: an item with properties including cover-image as per
         * https://idpf.github.io/epub-vocabs/package/item/#sec-cover-image
         *
         * If that doesn't work, try the EPUB 2 way - look for a section in the metadata
         * meta name=cover name=itemId
         */
        return manifest.items.firstOrNull {
            it.properties?.split(whitespaceRegex)?.contains("cover-image")  ?: false
        } ?: metadata.metas.firstOrNull { it.name == "cover" }?.content?.let { coverId ->
            manifest.items.firstOrNull { it.id == coverId }
        }

    }

    /**
     * Determine which item is the table of contents item.
     */
    fun tableOfContentItem(): Item? {
        val whiteSpaceRegex = Regex("\\s+")

        //Load and setup table of contents. As per the EPUB3 spec, an XHTML navigation
        //document is preferred, which will have the "toc nav" property. If that is not
        //found, then we will fallback to using the NCX
        val tocCandidates = manifest.items.filter {item ->
            item.properties?.split(whiteSpaceRegex)?.let { "toc" in it || "nav" in it } ?: false
        }
        val spineNcxCandidate: List<Item> = spine.toc?.let { spineTocid ->
            manifest.items.firstOrNull { it.id == spineTocid }?.let { listOf(it) } ?: emptyList()
        } ?: emptyList()
        val allTocCandidates = (tocCandidates + spineNcxCandidate)

        return allTocCandidates.firstOrNull {
            it.properties?.contains("nav") ?: false
        } ?: allTocCandidates.firstOrNull()
    }


    companion object {

        const val NS_OPF = "http://www.idpf.org/2007/opf"

        const val NS_DC = "http://purl.org/dc/elements/1.1/"
    }
}