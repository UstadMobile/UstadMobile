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
package com.ustadmobile.core.contentformats.epub.nav

import com.ustadmobile.xmlpullparserkmp.XmlPullParser
import com.ustadmobile.xmlpullparserkmp.XmlPullParserConstants
import com.ustadmobile.xmlpullparserkmp.XmlSerializer


/**
 * This class represents an EPUB navigation document as indicated by the OPF. It can be used to
 * load an EPUB3 XHTML navigation document or an EPUB2 NCX file.
 *
 * @author mike
 */
class EpubNavDocument {

    /**
     * Table of nav tags
     */
    private val navItems: MutableMap<String, EpubNavItem> = mutableMapOf()

    var ncxNavMap: EpubNavItem? = null
        private set

    /**
     * Vector of all navigation elements found
     */
    private val navElements: MutableList<EpubNavItem> = mutableListOf()


    /**
     * As per the EPUB navigation xhtml spec there can be multiple nav elements each with an epub:type
     * attribute. This method will attempt to get the EpubNavItem that has epub:type="toc".
     *
     * @return
     */
    val toc: EpubNavItem?
        get() {
            for (item in navElements) {
                if (item.navElEpubTypeAttr == null)
                    continue

                if (arrayOf(*item.navElEpubTypeAttr!!.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                                .contains(EPUB_NAV_DOCUMENT_TYPE_TOC))
                    return item
            }

            return null
        }

    /**
     * Load from the given XML Pull Parser. This is done in Namespace Aware Mode
     */
    fun load(xpp: XmlPullParser) {
        xpp.setFeature(XmlPullParserConstants.FEATURE_PROCESS_NAMESPACES, true)

        var evtType: Int = -1
        var currentNav: EpubNavItem? = null//represents the current nav tag
        var currentItem: EpubNavItem? = null
        var itemDepth = 0
        var tagName: String

        while ({evtType = xpp.next(); evtType}() != XmlPullParserConstants.END_DOCUMENT) {
            when (evtType) {
                XmlPullParserConstants.START_TAG -> {
                    tagName = xpp.getName()?: ""
                    if (tagName == "nav") {
                        currentNav = EpubNavItem(null, null, null, 0)
                        val navTypeAttr = xpp.getAttributeValue(NAMESPACE_OPS,
                                "type")
                        val idAttrVal = xpp.getAttributeValue(null, "id")

                        if (navTypeAttr != null)
                            currentNav.navElEpubTypeAttr = navTypeAttr

                        if (idAttrVal != null) {
                            currentNav.id = idAttrVal
                            navItems[idAttrVal] = currentNav
                        }

                        navElements.add(currentNav)
                    } else if(tagName.equals("navMap", true)) {
                        currentNav = EpubNavItem(null, null, null, 0)
                        ncxNavMap = currentNav
                    } else if (tagName == "li") {
                        currentItem = EpubNavItem(currentItem ?: currentNav, itemDepth)
                        itemDepth++
                    } else if (tagName == "a") {
                        currentItem?.href = xpp.getAttributeValue(null, "href")
                        if (xpp.next() == XmlPullParserConstants.TEXT) {
                            currentItem?.title = xpp.getText()
                        }
                    }else if(tagName.equals("navPoint", true)) {
                        //Epub 2.0 NCX navPoint also starts a new item
                        currentItem = EpubNavItem(currentItem ?: currentNav, itemDepth)
                    }else if(tagName =="text") {
                        if(xpp.next() == XmlPullParserConstants.TEXT) {
                            currentItem?.title = xpp.getText()
                        }
                    }else if(tagName =="content") {
                        currentItem?.href = xpp.getAttributeValue(null, "src")
                    }
                }

                XmlPullParserConstants.END_TAG -> {
                    if (xpp.getName() == "nav") {
                        currentNav = null
                    } else if (xpp.getName() == "li") {
                        currentItem = currentItem?.parent
                        itemDepth--
                    }else if(xpp.getName().equals("navPoint", true)) {
                        currentItem = currentItem?.parent
                        itemDepth--
                    }
                }
            }
        }
    }

    fun serialize(xs: XmlSerializer) {
        xs.startDocument("UTF-8", false)
        xs.setPrefix("", NAMESPACE_XHTML)
        xs.setPrefix("epub", NAMESPACE_OPS)

        xs.startTag(NAMESPACE_XHTML, "html")
                .startTag(NAMESPACE_XHTML, "head")
                .startTag(NAMESPACE_XHTML, "meta")
                .attribute(null, "charset", "UTF-8")
                .endTag(NAMESPACE_XHTML, "meta")
                .endTag(NAMESPACE_XHTML, "head")
                .startTag(NAMESPACE_XHTML, "body")

        for (navItem in navElements) {
            xs.startTag(NAMESPACE_XHTML, "nav")
            if (navItem.id != null)
                xs.attribute(null, "id", navItem.id!!)

            if (navItem.navElEpubTypeAttr != null)
                xs.attribute(NAMESPACE_OPS, "type",
                        navItem.navElEpubTypeAttr!!)

            xs.startTag(NAMESPACE_XHTML, "ol")
            for (childItem in navItem.getChildren()!!) {
                writeNavItem(childItem, xs)
            }
            xs.endTag(NAMESPACE_XHTML, "ol")
                    .endTag(NAMESPACE_XHTML, "nav")
        }

        xs.endTag(NAMESPACE_XHTML, "body")
        xs.endTag(NAMESPACE_XHTML, "html")
        xs.endDocument()
    }

    private fun writeNavItem(item: EpubNavItem, xs: XmlSerializer) {
        xs.startTag(NAMESPACE_XHTML, "li")
                .startTag(NAMESPACE_XHTML, "a")
                .attribute(null, "href", item.href!!)
                .text(item.title!!)
                .endTag(NAMESPACE_XHTML, "a")

        if (item.hasChildren()) {
            xs.startTag(NAMESPACE_XHTML, "ol")
            for (child in item.getChildren()!!) {
                writeNavItem(child, xs)
            }
            xs.endTag(NAMESPACE_XHTML, "ol")
        }

        xs.endTag(NAMESPACE_XHTML, "li")
    }


    fun getNavById(id: String): EpubNavItem? {
        return if (navItems.containsKey(id)) {
            navItems[id]
        } else {
            null
        }
    }

    fun EpubNavItem.findByHref(href: String): EpubNavItem? {
        if(this.href == href)
            return this

        return getChildren()?.asSequence()?.map { it.findByHref(href) }?.firstOrNull { it != null}
    }

    fun getNavByHref(href: String): EpubNavItem? {
        val result = navElements.asSequence().map { it.findByHref(href) }.firstOrNull { it != null }
                ?: ncxNavMap?.findByHref(href)
        return result
    }

    companion object {

        private val EPUB_NAV_DOCUMENT_TYPE_TOC = "toc"

        private val NAMESPACE_OPS = "http://www.idpf.org/2007/ops"

        val NAMESPACE_XHTML = "http://www.w3.org/1999/xhtml"
    }

}
