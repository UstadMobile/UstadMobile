/*
    This file is part of Ustad Mobile.

    Ustad Mobile Copyright (C) 2011-2014 UstadMobile Inc.

    Ustad Mobile is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version with the following additional terms:

    All names, links, and logos of Ustad Mobile and Toughra Technologies FZ
    LLC must be kept as they are in the original distribution.  If any new
    screens are added you must include the Ustad Mobile logo as it has been
    used in the original distribution.  You may not create any new
    functionality whose purpose is to diminish or remove the Ustad Mobile
    Logo.  You must leave the Ustad Mobile logo as the logo for the
    application to be used with any launcher (e.g. the mobile app launcher).

    If you want a commercial license to remove the above restriction you must
    contact us.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Ustad Mobile is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 */
package com.ustadmobile.core.contentformats.epub.opf

import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import org.kmp.io.KMPPullParser
import org.kmp.io.KMPSerializerParser
import org.kmp.io.KMPXmlParser

/**
 *
 * @author varuna
 */
class OpfDocument {

    private val spine: MutableList<OpfItem>

    private val manifestItems: MutableMap<String, OpfItem>

    private val coverImages = ArrayList<OpfItem>()

    /**
     * The item from the OPF that contains "nav" in it's properties.  As per the
     * EPUB spec there must be exactly one such item
     */
    /**
     * Return the opf item that represents the navigation document. This is the OPF item that
     * contains properties="nav". As per the spec, only one item is allowed to have this property.
     *
     * @return
     */
    var navItem: OpfItem? = null

    var title: String? = null

    var id: String? = null

    var description: String? = null

    var date: String? = null

    /*
     * the dc:identifier attribute as per
     * http://www.idpf.org/epub/30/spec/epub30-publications.html#sec-opf-metadata-identifiers-uid
     */
    private var uniqueIdentifier: String? = null

    private var links: MutableList<LinkElement>? = null

    internal var creators :MutableList<OpfCreator>? = null

    //As per the OPF spec a dc:language tag is required
    private val languages = mutableListOf<String>()

    /**
     * Gets an array of linear hrefs from the spine
     *
     * @return String array of all the HREFs that are in the linear spine order
     */
    val linearSpineHREFs: Array<String>
        get() {
            val spineHREFs = ArrayList<String>()

            for (i in spine.indices) {
                if (spine[i].isLinear!!) {
                    spineHREFs.add(spine[i].href!!)
                }
            }


            return spineHREFs.toTypedArray()
        }

    val numCreators: Int
        get() = if (creators != null) creators!!.size else 0

    class LinkElement {

        var rel: String? = null

        var mediaType: String? = null

        var href: String? = null

        var id: String? = null

        var refines: String? = null

        companion object {

            internal const val ATTR_REL = "rel"

            internal const val ATTR_HREF = "href"

            internal const val ATTR_MEDIA_TYPE = "media-type"

            internal const val ATTR_ID = "id"

            internal const val ATTR_REFINES = "refines"
        }
    }

    init {
        spine = ArrayList()
        manifestItems = HashMap()
    }

    /*
     * xpp: Parser of the OPF
     */
    fun loadFromOPF(xpp: KMPXmlParser, parseFlags: Int = PARSE_METADATA or PARSE_MANIFEST) {
        val parseMetadata = parseFlags and PARSE_METADATA == PARSE_METADATA
        val parseManifest = parseFlags and PARSE_MANIFEST == PARSE_MANIFEST


        var evtType = xpp.getEventType()
        var filename: String?
        var itemMediaType: String?
        var id: String?
        var properties: String?
        var idref: String?
        var isLinear: Boolean
        var isLinearStrVal: String?


        var inMetadata = false
        do {
            val tagName: String?
            val creator: OpfCreator
            val tagVal: String


            //If we are parsing the manifest
            if (parseManifest) {
                if (evtType == KMPPullParser.START_TAG) {
                    tagName = xpp.getName()
                    if (tagName != null && tagName == "item") {

                        filename = xpp.getAttributeValue(null, "href")!!
                        itemMediaType = xpp.getAttributeValue(null, "media-type")!!
                        id = xpp.getAttributeValue(null, "id")!!
                        properties = if( xpp.getAttributeValue(null, "properties").isNullOrEmpty())
                            "" else  xpp.getAttributeValue(null, "properties")

                        val item2 = OpfItem()
                        item2.href = filename
                        item2.mediaType = itemMediaType
                        item2.properties = properties
                        item2.id = id

                        /*
                         * As per the EPUB spec only one item should have this property
                         */
                        if (properties?.contains("nav")!!) {
                            navItem = item2
                        }
                        if (properties.contains("cover-image")) {
                            addCoverImage(item2)
                        }


                        manifestItems[id] = item2

                    } else if (xpp.getName() != null && xpp.getName() == "itemref") {
                        //for each itemRef in spine
                        idref = xpp.getAttributeValue(null, "idref")
                        isLinearStrVal = xpp.getAttributeValue(null, "linear")

                        val spineItem = manifestItems[idref]
                        if (spineItem != null) {
                            if (isLinearStrVal != null) {
                                val isLinearChar = isLinearStrVal[0]
                                isLinear = !((isLinearChar == 'n') or (isLinearChar == 'N'))
                                manifestItems[idref]?.isLinear = isLinear
                            }
                            spine.add(manifestItems[idref]!!)
                        } else {
                            UMLog.l(UMLog.WARN, 209, idref)
                        }
                    }
                }
            }

            if (parseMetadata) {
                if (evtType == KMPPullParser.START_TAG) {
                    if (uniqueIdentifier == null && xpp.getName() == "package") {
                        uniqueIdentifier = xpp.getAttributeValue(null,
                                "unique-identifier")
                    } else if (!inMetadata && xpp.getName() == "metadata") {
                        inMetadata = true
                    }

                    if (inMetadata) {
                        if (xpp.getName() == "dc:title") {
                            title = xpp.nextText()
                        }else if(xpp.getName() == "dc:date"){
                            date = xpp.nextText()
                        } else if (xpp.getName() == "dc:identifier") {
                            val idAttr = xpp.getAttributeValue(null, "id")
                            if (idAttr != null && idAttr == uniqueIdentifier) {
                                this.id = xpp.nextText()
                            }
                        } else if (xpp.getName() == "dc:description") {
                            description = xpp.nextText()
                        } else if (xpp.getName() == "link") {
                            val linkEl = LinkElement()
                            linkEl.href = xpp.getAttributeValue(null, LinkElement.ATTR_HREF)
                            linkEl.id = xpp.getAttributeValue(null, LinkElement.ATTR_ID)
                            linkEl.mediaType = xpp.getAttributeValue(null, LinkElement.ATTR_MEDIA_TYPE)
                            linkEl.rel = xpp.getAttributeValue(null, LinkElement.ATTR_REL)
                            linkEl.refines = xpp.getAttributeValue(null, LinkElement.ATTR_REFINES)
                            if (links == null)
                                links = mutableListOf()

                            links!!.add(linkEl)
                        } else if (xpp.getName() == "dc:creator") {
                            creator = OpfCreator()
                            creator.id = xpp.getAttributeValue(null, LinkElement.ATTR_ID)
                            if (xpp.next() == KMPPullParser.TEXT)
                                creator.creator = xpp.getText()

                            if (creators == null)
                                creators = ArrayList()

                            creators!!.add(creator)
                        } else if (xpp.getName() == "dc:language") {
                            if (xpp.next() == KMPPullParser.TEXT) {
                                tagVal = xpp.getText()!!
                                languages.add(tagVal)
                            }
                        }
                    }
                } else if (evtType == KMPPullParser.END_TAG) {
                    if (inMetadata && xpp.getName() == "metadata") {
                        inMetadata = false
                    }
                }
            }


            evtType = xpp.next()

        } while (evtType != KMPPullParser.END_DOCUMENT)
    }

    /**
     * Serialize this document to the given XmlSerializer
     *
     * @param xs XmlSerializer
     *
     * @throws IOException if an IOException occurs in the underlying IO
     */
    fun serialize(xs: KMPSerializerParser) {
        xs.startDocument("UTF-8", false)
        xs.setPrefix("", NAMESPACE_OPF)

        xs.startTag(NAMESPACE_OPF, "package")
        xs.attribute(null, "version", "3.0")
        xs.attribute(null, "unique-identifier", uniqueIdentifier!!)

        xs.setPrefix("dc", NAMESPACE_DC)
        xs.startTag(NAMESPACE_OPF, "metadata")

        xs.startTag(NAMESPACE_DC, "identifier")
        xs.attribute(null, LinkElement.ATTR_ID, uniqueIdentifier!!)
        xs.text(id!!)
        xs.endTag(NAMESPACE_DC, "identifier")

        xs.startTag(NAMESPACE_DC, "title")
        xs.text(title!!)
        xs.endTag(NAMESPACE_DC, "title")

        if(date != null){
            xs.startTag(NAMESPACE_DC, "date")
            xs.text(date!!)
            xs.endTag(NAMESPACE_DC, "date")
        }

        if(description != null){
            xs.startTag(NAMESPACE_DC, "description")
            xs.text(description!!)
            xs.endTag(NAMESPACE_DC, "description")
        }

        languages.forEach {
            xs.startTag(NAMESPACE_DC, "language")
            xs.text(it)
            xs.endTag(NAMESPACE_DC, "language")
        }


        if(creators != null){
            creators!!.forEach {
                if(it.creator != null){
                    xs.startTag(NAMESPACE_DC, "creator")
                    xs.attribute(null,LinkElement.ATTR_ID, it.id!!)
                    xs.text(it.creator!!)
                    xs.endTag(NAMESPACE_DC, "creator")
                }
            }
        }

        xs.endTag(NAMESPACE_OPF, "metadata")

        xs.startTag(NAMESPACE_OPF, "manifest")
        for (item in manifestItems.values) {
            xs.startTag(NAMESPACE_OPF, "item")
            xs.attribute(null, "id", item.id!!)
            xs.attribute(null, "href", item.href!!)
            xs.attribute(null, "media-type", item.mediaType!!)
            if (item.properties != null)
                xs.attribute(null, "properties", item.properties!!)
            xs.endTag(NAMESPACE_OPF, "item")
        }
        xs.endTag(NAMESPACE_OPF, "manifest")

        xs.startTag(NAMESPACE_OPF, "spine")
        for (item in spine) {
            xs.startTag(NAMESPACE_OPF, "itemref")
            xs.attribute(null, "idref", item.id!!)
            xs.endTag(NAMESPACE_OPF, "itemref")
        }
        xs.endTag(NAMESPACE_OPF, "spine")

        xs.endTag(NAMESPACE_OPF, "package")
        xs.endDocument()
    }


    fun getMimeType(filename: String): String? {
        val item = findItemByHref(filename)
        return item?.mediaType
    }

    private fun findItemByHref(href: String): OpfItem? {
        for (item in manifestItems.values) {
            if (href == item.href)
                return item
        }

        return null
    }

    /**
     * Find the position of a particular spine item
     *
     * @param href the href to find the position of in the spine (as it appears in the OPF (relative)
     * @return position of that item in the linear spine or -1 if not found
     */
    fun getLinearSpinePositionByHREF(href: String): Int {
        val linearSpine = linearSpineHREFs
        for (i in linearSpine.indices) {
            if (linearSpine[i] == href) {
                return i
            }
        }

        return -1
    }

    /**
     * Add a cover image item.
     *
     * @param coverImage OpfItem representing the cover image (including href and mime type)
     */
    fun addCoverImage(coverImage: OpfItem) {
        coverImages.add(coverImage)
    }

    /**
     * Get the cover image for this publication.
     *
     * @param mimeType Preferred mime type (unimplemented)
     *
     * @return OpfItem representing the cover image
     */
    fun getCoverImage(mimeType: String): OpfItem? {
        return if (coverImages.isEmpty()) null else coverImages[0]

    }

    fun getCoverImages(): List<OpfItem> {
        return coverImages
    }

    fun getLinks(): List<LinkElement>? {
        return links
    }

    fun getCreators(): List<OpfCreator>? {
        return creators
    }

    fun getCreator(index: Int): OpfCreator {
        return creators!![index]
    }

    /**
     * Return a Vector of String objects containing the languages as per dc:language tags that were
     * found on this OPF, in the order they appeared in the declaration.
     *
     * @return Vector of language codes as Strings.
     */
    fun getLanguages(): List<String> {
        return languages
    }

    fun getSpine(): MutableList<OpfItem> {
        return spine
    }

    /**
     * Get map of manifest items. Mapped id to item
     *
     * @return map of manifest items
     */
    fun getManifestItems(): MutableMap<String, OpfItem> {
        return manifestItems
    }

    companion object {

        private const val NAMESPACE_OPF = "http://www.idpf.org/2007/opf"

        private const val NAMESPACE_DC = "http://purl.org/dc/elements/1.1/"

        /**
         * Flag value to indicate we should parse the metadata (e.g. title, identifier, description)
         */
        const val PARSE_METADATA = 1

        /**
         * Flag value to indicate we should parse the manifest
         */
        const val PARSE_MANIFEST = 2

        fun getExtension(filename: String): String? {
            val dotPos = filename.lastIndexOf('.')
            return if (dotPos != -1) filename.substring(dotPos + 1) else null
        }
    }

}
