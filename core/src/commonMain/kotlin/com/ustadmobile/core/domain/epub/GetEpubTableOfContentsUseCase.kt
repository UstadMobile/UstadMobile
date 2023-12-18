package com.ustadmobile.core.domain.epub

import com.ustadmobile.core.contentformats.epub.nav.ListItem
import com.ustadmobile.core.contentformats.epub.nav.NavigationDocument
import com.ustadmobile.core.contentformats.epub.ncx.NavPoint
import com.ustadmobile.core.contentformats.epub.ncx.NcxDocument
import com.ustadmobile.core.contentformats.epub.opf.Item
import com.ustadmobile.core.contentformats.epub.opf.PackageDocument
import com.ustadmobile.core.viewmodel.epubcontent.EpubTocItem
import kotlinx.atomicfu.atomic
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML

/**
 * Handle getting the Epub table of contents. This is used by the EpubViewModel itself and the
 * import plugin when validating epubs
 */
class GetEpubTableOfContentsUseCase(
    val xml: XML,
) {

    private val tocItemAtomicIds = atomic(0)

    /**
     * Convert a Epub Nav document to the common EpubTocItem
     */
    private fun ListItem.toEpubTocItem(
        indentLevel: Int,
        parentUids: Set<Int>,
    ) : EpubTocItem {
        val uid = tocItemAtomicIds.incrementAndGet()
        val uidsForChildren = parentUids + uid
        return EpubTocItem(
            uid = tocItemAtomicIds.incrementAndGet(),
            label = anchor?.content ?: span?.content ?: "",
            href = anchor?.href,
            children = this.orderedList?.listItems?.map {
                it.toEpubTocItem(indentLevel + 1, uidsForChildren)
            } ?: emptyList(),
            indentLevel = indentLevel,
            parentUids = parentUids,
        )
    }

    /**
     * Convert a NCX navpoint to the common EpubTocItem
     */
    private fun NavPoint.toTocItem(
        indentLevel: Int,
        parentUids: Set<Int>,
    ): EpubTocItem {
        val uid = tocItemAtomicIds.incrementAndGet()
        val uidsForChildren = parentUids + uid
        return EpubTocItem(
            uid = uid,
            label = navLabels.firstOrNull()?.text?.content ?: "",
            href = content.src,
            indentLevel = indentLevel,
            parentUids = parentUids,
            children = this.childPoints.map {
                it.toTocItem(indentLevel + 1, uidsForChildren)
            }
        )
    }

    /**
     * Get the table of contents as a list of EpubTocItem. If a table of contents is not found
     */
    suspend operator fun invoke(
        opfPackage: PackageDocument,
        readItemText: suspend (Item) -> String
    ) : List<EpubTocItem>? {
        val tocToUse = opfPackage.tableOfContentItem()

        return if(
            tocToUse != null && tocToUse.mediaType.startsWith("application/xhtml")
        ) {
            val docStr = readItemText(tocToUse)
            val navDoc: NavigationDocument = xml.decodeFromString(docStr)
            navDoc.bodyElement.navigationElements
                .first().orderedList.listItems
                .flatMap { listItem ->
                    listItem.toEpubTocItem(0, emptySet()).let { listOf(it) + it.children }
                }
        }else if(tocToUse != null && tocToUse.mediaType == NcxDocument.MIMETYPE_NCX) {
            val docStr = readItemText(tocToUse)
            val ncxDoc: NcxDocument = xml.decodeFromString(docStr)
            ncxDoc.navMap.navPoints.flatMap { navPoint ->
                navPoint.toTocItem(0, emptySet()).let { listOf(it) + it.children }
            }
        }else {
            null
        }
    }

}