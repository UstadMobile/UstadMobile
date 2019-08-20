/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.contentformats.epub.nav

import com.ustadmobile.core.util.UMFileUtil

/**
 * EpubNavItem may represent a nav item itself or an li item.
 *
 * @author mike
 */
class EpubNavItem(
        /**
         * The title of this table of contents entry - as per the text found within the &lt;a href tag.
         */
        var title: String?,
        /**
         * The href found on the a tag within the li item
         */
        var href: String?,
        /**
         * The parent item of this item, if any
         */
        var parent: EpubNavItem?, var depth: Int) {

    /**
     * The id of this element (if any)
     */
    var id: String? = null

    /**
     * The child items of this item, if any
     */
    private var children: MutableList<EpubNavItem>? = null

    /**
     * The EPUB navigation XHTML contains one or more nav html elements, which should have an (epub
     * namespace) type attribute. This string is the value that type attribute. e.g. for
     * &lt;nav epub:type="toc"&gt; this would contain the string "toc"
     *
     * @return if this EpubNavItem represents a nav element, then this is the value of the type
     * attribute, otherwise null
     */
    var navElEpubTypeAttr: String? = null

    init {
        if (parent != null) {
            parent!!.addChild(this)
        }
    }

    constructor(parent: EpubNavItem?, depth: Int) : this(null, null, parent, depth) {}

    fun getChildren(): MutableList<EpubNavItem>? {
        return children
    }

    fun setChildren(children: MutableList<EpubNavItem>) {
        this.children = children
    }

    override fun toString(): String {
        var sb = StringBuilder()
        for (i in 0 until depth) {
            sb.append(' ')
        }
        return sb.append(title).toString()
    }

    fun addChild(child: EpubNavItem) {
        if (children == null) {
            children = mutableListOf()
        }
        children!!.add(child)
    }

    fun size(): Int {
        return if (children != null) children!!.size else 0
    }

    fun getChild(index: Int): EpubNavItem {
        return children!![index]
    }

    fun hasChildren(): Boolean {
        return children != null && !children!!.isEmpty()
    }

    companion object {

        /**
         * Given a Vector of EPUBNavItems find the index of the one that contains a
         * given HREF - ignoring the anchor section (e.g. #foo) of nav items in the
         * vector
         *
         * @param href href To find
         * @param vector Vector containing EPUBNavItems
         *
         * @return index of the given HREF or -1 if not found
         */
        fun findItemInVectorByHref(href: String, vector: List<*>): Int {
            var itemHref: String?
            for (i in vector.indices) {
                itemHref = (vector.elementAt(i) as EpubNavItem).href
                if (itemHref != null && UMFileUtil.stripAnchorIfPresent(itemHref) == href) {
                    return i
                }
            }

            return -1
        }
    }

}
