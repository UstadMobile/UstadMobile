/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.contentformats.epub.nav;

import com.ustadmobile.core.util.UMFileUtil;

import java.util.List;
import java.util.Vector;

/**
 * EpubNavItem may represent a nav item itself or an li item.
 *
 * @author mike
 */
public class EpubNavItem {

    /**
     * The title of this table of contents entry - as per the text found within the &lt;a href tag.
     */
    private String title;

    /**
     * The href found on the a tag within the li item
     */
    private String href;

    /**
     * The parent item of this item, if any
     */
    private EpubNavItem parent;

    /**
     * The id of this element (if any)
     */
    private String id;

    /**
     * The child items of this item, if any
     */
    private List<EpubNavItem> children;
    
    private int depth;

    private String navElEpubTypeAttr;

    /**
     * If this represents a nav node,
     */
    //public String[] epubNavType;
        
    public EpubNavItem(String title, String href, EpubNavItem parent, int depth) {
        this.title = title;
        this.href = href;
        this.parent = parent;
        if(parent != null) {
            parent.addChild(this);
        }
        this.depth = depth;
    }
    
    public EpubNavItem(EpubNavItem parent, int depth) {
        this(null, null, parent, depth);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public EpubNavItem getParent() {
        return parent;
    }

    public void setParent(EpubNavItem parent) {
        this.parent = parent;
    }

    public List<EpubNavItem> getChildren() {
        return children;
    }

    public void setChildren(List<EpubNavItem> children) {
        this.children = children;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The EPUB navigation XHTML contains one or more nav html elements, which should have an (epub
     * namespace) type attribute. This string is the value that type attribute. e.g. for
     * &lt;nav epub:type="toc"&gt; this would contain the string "toc"
     *
     * @return if this EpubNavItem represents a nav element, then this is the value of the type
     * attribute, otherwise null
     */
    public String getNavElEpubTypeAttr() {
        return navElEpubTypeAttr;
    }

    public void setNavElEpubTypeAttr(String navElEpubTypeAttr) {
        this.navElEpubTypeAttr = navElEpubTypeAttr;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < depth; i++) {
            sb.append(' ');
        }
        return sb.append(title).toString();
    }
    
    public void addChild(EpubNavItem child) {
        if(children == null) {
            children = new Vector<>();
        }
        children.add(child);
    }

    public int size() {
        return children != null ? children.size() : 0;
    }

    public EpubNavItem getChild(int index) {
        return children.get(index);
    }
    
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

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
    public static int findItemInVectorByHref(String href, Vector vector) {
        String itemHref;
        for(int i = 0; i < vector.size(); i++) {
            itemHref = ((EpubNavItem)vector.elementAt(i)).href;
            if(itemHref != null && UMFileUtil.stripAnchorIfPresent(itemHref).equals(href)) {
                return i;
            }
        }
        
        return -1;
    }

}
