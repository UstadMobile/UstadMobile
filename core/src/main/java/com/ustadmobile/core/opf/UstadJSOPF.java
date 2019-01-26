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
package com.ustadmobile.core.opf;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author varuna
 */
public class UstadJSOPF {

    private List<UstadJSOPFItem> spine;

    private Map<String, UstadJSOPFItem> manifestItems;

    private List<UstadJSOPFItem> coverImages = new ArrayList<>();
    
    /**
     * The item from the OPF that contains "nav" in it's properties.  As per the 
     * EPUB spec there must be exactly one such item
     */
    public UstadJSOPFItem navItem;
    
    public String title;
    
    public String id;

    public String description;

    private List<LinkElement> links;

    private List<UstadJSOPFCreator> creators;

    //As per the OPF spec a dc:language tag is required
    private List<String> languages = new Vector<>();
    
    /**
     * Flag value to indicate we should parse the metadata (e.g. title, identifier, description)
     */
    public static final int PARSE_METADATA = 1;
    
    /**
     * Flag value to indicate we should parse the manifest
     */
    public static final int PARSE_MANIFEST = 2;

    public static String getExtension(String filename) {
        int dotPos = filename.lastIndexOf('.');
        return dotPos != -1 ? filename.substring(dotPos + 1) : null;
    }

    public static class LinkElement {

        static final String ATTR_REL = "rel";

        static final String ATTR_HREF = "href";

        static final String ATTR_MEDIA_TYPE = "media-type";

        static final String ATTR_ID = "id";

        static final String ATTR_REFINES = "refines";

        private String rel;

        private String mediaType;

        private String href;

        private String id;

        private String refines;

        public String getRel() {
            return rel;
        }

        public void setRel(String rel) {
            this.rel = rel;
        }

        public String getMediaType() {
            return mediaType;
        }

        public void setMediaType(String mediaType) {
            this.mediaType = mediaType;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getRefines() {
            return refines;
        }

        public void setRefines(String refines) {
            this.refines = refines;
        }
    }
    
    public UstadJSOPF() {
        spine = new ArrayList<>();
        manifestItems = new HashMap<>();
    }
    
    
    public void loadFromOPF(XmlPullParser xpp) throws XmlPullParserException, IOException {
        loadFromOPF(xpp, PARSE_METADATA | PARSE_MANIFEST);
    }
    
    /*
     * xpp: Parser of the OPF
     */
    public void loadFromOPF(XmlPullParser xpp, int parseFlags) throws XmlPullParserException, IOException {
        boolean parseMetadata = (parseFlags & PARSE_METADATA) == PARSE_METADATA;
        boolean parseManifest = (parseFlags & PARSE_MANIFEST) == PARSE_MANIFEST;
        
        
        int evtType = xpp.getEventType();
        String filename=null;
        String itemMime=null;
        String id=null;
        String properties=null;
        String idref=null;
        boolean isLinear = true;
        String isLinearStrVal = null;
        
        /*
         * the dc:identifier attribute as per 
         * http://www.idpf.org/epub/30/spec/epub30-publications.html#sec-opf-metadata-identifiers-uid
         */
        String uniqueIdentifier = null;

        
        boolean inMetadata = false;
        do
        {
            filename=null;
            itemMime=null;
            id=null;
            properties=null;
            idref=null;
            isLinear = true;
            isLinearStrVal = null;
            String tagName;
            UstadJSOPFCreator creator;
            String tagVal;
            
                        
            
            //If we are parsing the manifest
            if(parseManifest) {
                if(evtType == XmlPullParser.START_TAG){
                    tagName = xpp.getName();
                    if(tagName != null && tagName.equals("item")){

                        filename=xpp.getAttributeValue(null, "href");
                        itemMime=xpp.getAttributeValue(null, "media-type");
                        id = xpp.getAttributeValue(null, "id");
                        properties = xpp.getAttributeValue(null, "properties");

                        UstadJSOPFItem item2 = new UstadJSOPFItem();
                        item2.href = filename;
                        item2.mimeType = itemMime;
                        item2.properties = properties;
                        item2.id = id;

                        /*
                         * As per the EPUB spec only one item should have this property
                         */
                        if(properties != null && properties.contains("nav")) {
                            navItem = item2;
                        }
                        if(properties != null && properties.contains("cover-image")) {
                            addCoverImage(item2);
                        }


                        manifestItems.put(id, item2);

                    }else if(xpp.getName() != null && xpp.getName().equals("itemref")){
                        //for each itemRef in spine
                        idref=xpp.getAttributeValue(null, "idref");
                        isLinearStrVal = xpp.getAttributeValue(null, "linear");

                        UstadJSOPFItem spineItem = manifestItems.get(idref);
                        if(spineItem != null) {
                            if(isLinearStrVal != null) {
                                char isLinearChar = isLinearStrVal.charAt(0);
                                isLinear = !(isLinearChar == 'n' | isLinearChar == 'N');
                                manifestItems.get(idref).linear = isLinear;
                            }
                            spine.add(manifestItems.get(idref));
                        }else {
                            UstadMobileSystemImpl.l(UMLog.WARN, 209, idref);
                        }
                    }
                }
            }
            
            if(parseMetadata) {
                if(evtType == XmlPullParser.START_TAG) {
                    if(uniqueIdentifier == null && xpp.getName().equals("package")) {
                        uniqueIdentifier = xpp.getAttributeValue(null, 
                                "unique-identifier");
                    }else if(!inMetadata&& xpp.getName().equals("metadata")) {
                        inMetadata = true;
                    }
                    
                    if(inMetadata) {
                        if(xpp.getName().equals("dc:title")) {
                            title = xpp.nextText();
                        }else if(xpp.getName().equals("dc:identifier")) {
                            String idAttr = xpp.getAttributeValue(null, "id");
                            if(idAttr != null && idAttr.equals(uniqueIdentifier)) {
                                this.id = xpp.nextText();
                            }
                        }else if(xpp.getName().equals("dc:description")) {
                            description = xpp.nextText();
                        }else if(xpp.getName().equals("link")) {
                            LinkElement linkEl = new LinkElement();
                            linkEl.href = xpp.getAttributeValue(null, LinkElement.ATTR_HREF);
                            linkEl.id = xpp.getAttributeValue(null, LinkElement.ATTR_ID);
                            linkEl.mediaType = xpp.getAttributeValue(null, LinkElement.ATTR_MEDIA_TYPE);
                            linkEl.rel = xpp.getAttributeValue(null, LinkElement.ATTR_REL);
                            linkEl.refines = xpp.getAttributeValue(null, LinkElement.ATTR_REFINES);
                            if(links == null)
                                links = new Vector<>();

                            links.add(linkEl);
                        }else if(xpp.getName().equals("dc:creator")) {
                            creator = new UstadJSOPFCreator();
                            creator.setId(xpp.getAttributeValue(null, LinkElement.ATTR_ID));
                            if(xpp.next() == XmlPullParser.TEXT)
                                creator.setCreator(xpp.getText());

                            if(creators == null)
                                creators = new ArrayList<>();

                            creators.add(creator);
                        }else if(xpp.getName().equals("dc:language")) {
                            if(xpp.next() == XmlPullParser.TEXT) {
                                tagVal = xpp.getText();
                                languages.add(tagVal);
                            }
                        }
                    }
                }else if(evtType == XmlPullParser.END_TAG) {
                    if(inMetadata && xpp.getName().equals("metadata")) {
                        inMetadata = false;
                    }
                }
            }
            
            
            evtType = xpp.next();
            
        }while(evtType != XmlPullParser.END_DOCUMENT);
    }
    
    public String getMimeType(String filename) {
        UstadJSOPFItem item = findItemByHref(filename);
        if(item != null)
            return item.getMimeType();
        else
            return null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private UstadJSOPFItem findItemByHref(String href) {
        for(UstadJSOPFItem item : manifestItems.values()) {
            if(href.equals(item.getHref()))
                return item;
        }

        return null;
    }

    /**
     * Gets an array of linear hrefs from the spine
     * 
     * @return String array of all the HREFs that are in the linear spine order
     */
    public String[] getLinearSpineHREFs() {
        List<String> spineHREFs = new ArrayList<>();

        for(int i = 0; i < spine.size(); i++) {
            if(spine.get(i).linear) {
                spineHREFs.add(spine.get(i).href);
            }
        }


        return spineHREFs.toArray(new String[0]);
    }
    
     /**
     * Find the position of a particular spine item
     * 
     * @param href the href to find the position of in the spine (as it appears in the OPF (relative)
     * @return position of that item in the linear spine or -1 if not found
     */
    public int getLinearSpinePositionByHREF(String href) {
        String[] linearSpine = getLinearSpineHREFs();
        for(int i = 0; i < linearSpine.length; i++) {
            if(linearSpine[i].equals(href)) {
                return i;
            }
        }
        
        return -1;
    }

    /**
     * Add a cover image item.
     *
     * @param coverImage UstadJSOPFItem representing the cover image (including href and mime type)
     */
    public void addCoverImage(UstadJSOPFItem coverImage) {
        coverImages.add(coverImage);
    }

    /**
     * Get the cover image for this publication.
     *
     * @param mimeType Preferred mime type (unimplemented)
     *
     * @return UstadJSOPFItem representing the cover image
     */
    public UstadJSOPFItem getCoverImage(String mimeType) {
        if(coverImages == null || coverImages.isEmpty())
            return null;

        return coverImages.get(0);
    }

    public List<UstadJSOPFItem> getCoverImages() {
        return coverImages;
    }

    public List<LinkElement> getLinks() {
        return links;
    }


    /**
     * Return the opf item that represents the navigation document. This is the OPF item that
     * contains properties="nav". As per the spec, only one item is allowed to have this property.
     *
     * @return
     */
    public UstadJSOPFItem getNavItem() {
        return navItem;
    }

    public List<UstadJSOPFCreator> getCreators() {
        return creators;
    }

    public UstadJSOPFCreator getCreator(int index) {
        return creators.get(index);
    }

    public int getNumCreators() {
        return creators != null ? creators.size() : 0;
    }

    /**
     * Return a Vector of String objects containing the languages as per dc:language tags that were
     * found on this OPF, in the order they appeared in the declaration.
     *
     * @return Vector of language codes as Strings.
     */
    public List<String> getLanguages() {
        return languages;
    }

    public List<UstadJSOPFItem> getSpine() {
        return spine;
    }

}
