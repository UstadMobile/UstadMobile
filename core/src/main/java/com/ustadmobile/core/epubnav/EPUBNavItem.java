/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.epubnav;

import com.ustadmobile.core.util.UMFileUtil;

import java.util.Vector;

/**
 * EPUBNavItem may represent a nav item itself or an li item.
 *
 * @author mike
 */
public class EPUBNavItem {

    /**
     * The title of this table of contents entry - as per the text found within the &lt;a href tag.
     */
    public String title;

    /**
     * The href found on the a tag within the li item
     */
    public String href;

    /**
     * The parent item of this item, if any
     */
    public EPUBNavItem parent;

    /**
     * The child items of this item, if any
     */
    public Vector children;
    
    public int depth;

    /**
     * If this represents a nav node,
     */
    public String[] epubNavType;
        
    public EPUBNavItem(String title, String href, EPUBNavItem parent, int depth) {
        this.title = title;
        this.href = href;
        this.parent = parent;
        if(parent != null) {
            parent.addChild(this);
        }
        this.depth = depth;
    }
    
    public EPUBNavItem(EPUBNavItem parent, int depth) {
        this(null, null, parent, depth);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < depth; i++) {
            sb.append(' ');
        }
        return sb.append(title).toString();
    }
    
    public void addChild(EPUBNavItem child) {
        if(children == null) {
            children = new Vector();
        }
        children.addElement(child);
    }

    public int size() {
        return children != null ? children.size() : 0;
    }

    public EPUBNavItem getChild(int index) {
        return (EPUBNavItem)children.elementAt(index);
    }
    
    public boolean hasChildren() {
        return children != null;
    }
    
    public Vector getChildrenRecursive(Vector results) {
        if(title != null) {
            results.addElement(this);
        }
        
        if(children != null) {
            for(int i = 0; i < children.size(); i++) {
                ((EPUBNavItem)children.elementAt(i)).getChildrenRecursive(results);
            }
        }
        
        return results;
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
            itemHref = ((EPUBNavItem)vector.elementAt(i)).href;
            if(itemHref != null && UMFileUtil.stripAnchorIfPresent(itemHref).equals(href)) {
                return i;
            }
        }
        
        return -1;
    }

    public void setEpubNavType(String epubNavTypeAttr) {
//        TODO: check for white space
        epubNavType = UMFileUtil.splitString(epubNavTypeAttr, ' ');
    }

    
    /**
     * Recursively searches for the given HREF.  Will strip off and ignore the
     * anchor section of this navitem.
     *
     * @param href The href to find
     * @return the nav item that matches the href, or null if not found
     */
    /* Commented until we see if htis is needed for Droid etc.
    public EPUBNavItem findItemByHREF(String href) {
        EPUBNavItem result = null;

        if(this.href != null && UMFileUtil.stripAnchorIfPresent(href).equals(href)) {
            return this;
        }

        if(children != null) {
            for(int i = 0; i < children.size() && result == null; i++) {
                result = ((EPUBNavItem)children.elementAt(i)).findItemByHREF(href);
            }
        }

        return result;
    }
    */

}
