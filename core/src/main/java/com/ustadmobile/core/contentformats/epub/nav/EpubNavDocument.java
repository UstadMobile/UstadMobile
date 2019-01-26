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
package com.ustadmobile.core.contentformats.epub.nav;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * This class represents an EPUB navigation document as indicated by the OPF
 * 
 * @author mike
 */
public class EpubNavDocument {
    
    /**
     * Table of nav tags
     */
    private Map<String, EpubNavItem> navItems;

    /**
     * Vector of all navigation elements found
     */
    private List<EpubNavItem> navElements;

    private static final String EPUB_NAV_DOCUMENT_TYPE_TOC = "toc";

    private static final String DOCTYPE_OPS_NAMESPACE = "http://www.idpf.org/2007/ops";

    public static final String DOCTYPE_XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";
    
    public EpubNavDocument() {
        navItems = new Hashtable<>();
        navElements = new Vector<>();
    }
    
    public void load(XmlPullParser xpp) throws XmlPullParserException, IOException{
        xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

        int evtType;
        EpubNavItem currentNav = null;//represents the current nav tag
        EpubNavItem currentItem = null;
        int itemDepth = 0;
        String tagName;
        
        while((evtType = xpp.next()) != XmlPullParser.END_DOCUMENT) {
            switch(evtType) {
                case XmlPullParser.START_TAG:
                    tagName = xpp.getName();
                    if(tagName.equals("nav")) {
                        currentNav = new EpubNavItem(null, null, null, 0);
                        String navTypeAttr = xpp.getAttributeValue(DOCTYPE_OPS_NAMESPACE,
                                "type");
                        String idAttrVal = xpp.getAttributeValue(null, "id");

                        if(navTypeAttr != null)
                            currentNav.setNavElEpubTypeAttr(navTypeAttr);

                        if(idAttrVal != null) {
                            currentNav.setId(idAttrVal);
                            navItems.put(idAttrVal, currentNav);
                        }

                        navElements.add(currentNav);
                    }else if(tagName.equals("li")) {
                        currentItem = new EpubNavItem(currentItem != null ?
                            currentItem : currentNav, itemDepth);
                        itemDepth++;
                    }else if(tagName.equals("a")) {
                        currentItem.setHref(xpp.getAttributeValue(null, "href"));
                        if(xpp.next() == XmlPullParser.TEXT) {
                            currentItem.setTitle(xpp.getText());
                        }
                    }
                    break;
                    
                case XmlPullParser.END_TAG:
                    if(xpp.getName().equals("nav")) {
                        currentNav = null;
                    }else if(xpp.getName().equals("li")) {
                        currentItem = currentItem.getParent();
                        itemDepth--;
                    }
                    
                    break;
            }
        }
    }

    public void serialize(XmlSerializer xs) throws IOException{
        xs.startDocument("UTF-8", false);
        xs.setPrefix(null, DOCTYPE_XHTML_NAMESPACE);
        xs.setPrefix("epub", DOCTYPE_OPS_NAMESPACE);

        xs.startTag(null, "html")
            .startTag(null, "head")
                .startTag(null, "meta")
                    .attribute(null, "charset", "UTF-8")
                .endTag(null, "meta")
            .endTag(null, "head")
            .startTag(null, "body");

        for(EpubNavItem navItem : navElements) {
            xs.startTag(null, "nav");
            if(navItem.getId() != null)
                xs.attribute(null, "id", navItem.getId());

            if(navItem.getNavElEpubTypeAttr() != null)
                xs.attribute(DOCTYPE_OPS_NAMESPACE, "type",
                        navItem.getNavElEpubTypeAttr());

            xs.startTag(null, "ol");
            for(EpubNavItem childItem : navItem.getChildren()){
                writeNavItem(childItem, xs);
            }
            xs.endTag(null, "ol")
                .endTag(null, "nav");
        }

        xs.endTag(null, "body");
        xs.endTag(null, "html");
        xs.endDocument();
    }

    private void writeNavItem(EpubNavItem item, XmlSerializer xs) throws IOException {
        xs.startTag(null, "li")
            .startTag(null, "a")
                .attribute(null, "href", item.getHref())
                .text(item.getTitle())
            .endTag(null, "a");

        if(item.hasChildren()) {
            xs.startTag(null, "ol");
            for(EpubNavItem child : item.getChildren()){
                writeNavItem(child, xs);
            }
            xs.endTag(null, "ol");
        }

        xs.endTag(null, "li");
    }


    /**
     * As per the EPUB navigation xhtml spec there can be multiple nav elements each with an epub:type
     * attribute. This method will attempt to get the EpubNavItem that has epub:type="toc".
     *
     * @return
     */
    public EpubNavItem getToc() {
        for(EpubNavItem item : navElements) {
            if(item.getNavElEpubTypeAttr() == null)
                continue;

            if(Arrays.asList(item.getNavElEpubTypeAttr().split("\\s+"))
                    .contains(EPUB_NAV_DOCUMENT_TYPE_TOC))
                return item;
        }

        return null;
    }



    
    public EpubNavItem getNavById(String id) {
        if(navItems.containsKey(id)) {
            return navItems.get(id);
        }else {
            return null;
        }
    }
    
}
