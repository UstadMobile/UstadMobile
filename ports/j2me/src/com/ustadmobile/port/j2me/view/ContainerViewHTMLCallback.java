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
package com.ustadmobile.port.j2me.view;

import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.html.AsyncDocumentRequestHandler;
import com.sun.lwuit.html.DefaultHTMLCallback;
import com.sun.lwuit.html.DocumentInfo;
import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.html.HTMLElement;
import com.ustadmobile.core.controller.ContainerController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import com.ustadmobile.port.j2me.view.idevice.EXEQuizIdevice;
import com.ustadmobile.port.j2me.view.idevice.IdeviceJ2ME;
import com.ustadmobile.port.j2me.view.idevice.TextEntryIdevice;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.json.me.JSONObject;
import org.json.me.JSONException;

/**
 *
 * This LWUIT HTML Callback class handles requests to play media files (e.g. 
 * autoplay sounds etc) and manipulates the dom to show feedback when MCQ
 * answers are selected.  On devices that support Javascript this is all handled
 * by Javascript... but that won't work here on J2ME.
 * 
 * @author mike
 */
public class ContainerViewHTMLCallback extends DefaultHTMLCallback {

    private ContainerViewJ2ME view;
    
    boolean fixedPage = true;

    static Hashtable mediaExtensions;
    
    String containerTinCanID;
    
    Object context;
    
    private String registrationUUID;
    
    private JSONObject state;
    
    public static final String IDEVICE_CLASS_TEXTENTRY = "TextEntryIdevice";
    
    public static final String TEXTENTRY_INPUTEL_PREFIX = "exe_tei_textel_";
    
    private Hashtable pageIdevices;
    
    public static final String[] IDEVICE_CSS_CLASSES = new String[]{"Idevice",
        "iDevice_wrapper"};
    
    /**
     * Marked as true when the dom has been changed and then refreshDOM needs
     * to be called
     */
    private boolean domInvalid;
    
    /**
     * Hashtable of idevice classes to the class that will be used to handle it
     */
    private static Hashtable ideviceClasses;
        
    static {
        mediaExtensions = new Hashtable();
        mediaExtensions.put("mp3", "audio/mpeg");
        
        ideviceClasses = new Hashtable();
        ideviceClasses.put("TextEntryIdevice", TextEntryIdevice.class);
        ideviceClasses.put("MultichoiceIdevice", EXEQuizIdevice.class);
    }
    
    

    public static final int[] IDEVICE_TAG_IDS = new int[] { HTMLElement.TAG_DIV, 
        HTMLElement.TAG_SECTION, HTMLElement.TAG_ARTICLE};
    
    public static final int[] MCQ_FORM_TAGIDS = new int[] { HTMLElement.TAG_FORM };
    
    public ContainerViewHTMLCallback(String containerTinCanID, Object context) {
        super();
        this.containerTinCanID = containerTinCanID;
        this.context = context;
    }
    
    public ContainerViewHTMLCallback(ContainerViewJ2ME view) {
        this.view = view;
        this.context = view.getContext();
    }

    
    /**
     * Returns a hashtable mapped as id to Idevice object of the idevices that
     * are on the page
     * 
     * @return Hashtable as above
     */
    public Hashtable getPageIdevices() {
        return pageIdevices;
    }
    
    /**
     * Get the registration UUID to be used for Experience API statements
     * 
     * @return the registration UUID to be used for Experience API statements
     */
    public String getRegistrationUUID() {
        return registrationUUID;
    }

    /**
     * Set the registration UUID to be used for Experience API statements
     * 
     * @param registrationUUID the registration UUID to be used for Experience API statements
     */
    public void setRegistrationUUID(String registrationUUID) {
        this.registrationUUID = registrationUUID;
    }
    
    public void setState(JSONObject state) {
        this.state = state;
    }
    
    public JSONObject getState() {
        return state;
    }
    
    public void invalidateDOM() {
        domInvalid = true;
    }
    
    protected void refreshDOM(HTMLComponent htmlC) {
        htmlC.refreshDOM();
        domInvalid = false;
    }
    
    /**
     * Return the full TinCan ID of the current page: e.g.
     * epub:uuid/page-item-id
     * 
     * @return Full TinCan ID for the page as above
     */
    public String getPageTinCanID(HTMLComponent htmlC) {
        if(containerTinCanID == null && view != null) {
            containerTinCanID = view.getContainerTinCanID();
        }
        
        String pageIdSection = null;
        if(view != null) {
            pageIdSection = view.getCurrentPageOPFId();
        }else if(htmlC.getPageURL() != null) {
            pageIdSection = UMFileUtil.getFilename(htmlC.getPageURL());
        }
        
        return containerTinCanID + '/' + pageIdSection;
    }

    /**
     * Hide extras made by eXeLearning that would otherwise be hidden by CSS
     *
     * @param htmlC HMTLComponent we are operating with
     * @return true if modifications were made to the DOM, false otherwise
     */
    private boolean hideExtras(HTMLComponent htmlC) {
        int[] extraTagTypeIDS = new int[]{HTMLElement.TAG_DIV, 
            HTMLElement.TAG_SECTION, HTMLElement.TAG_LABEL, HTMLElement.TAG_H1};
        final String[] cssClassesToHide = new String[] {"js-sr-av", "sr-av", 
            "iDevice_solution"};
        
        Vector tags;
        int i;
        int j;
        int h;
        HTMLElement currentEl;

        int removed = 0;

        for (i = 0; i < extraTagTypeIDS.length; i++) {
            tags = htmlC.getDOM().getDescendantsByTagId(extraTagTypeIDS[i]);
            for (j = 0; j < tags.size(); j++) {
                currentEl = (HTMLElement) tags.elementAt(j);
                String elClasses = currentEl.getAttributeById(HTMLElement.ATTR_CLASS);
                if(elClasses != null) {
                    for(h = 0; h < cssClassesToHide.length; h++) {
                        if(elClasses.indexOf(cssClassesToHide[h]) != -1) {
                            currentEl.getParent().removeChildAt(
                                currentEl.getParent().getChildIndex(currentEl));
                            removed++;
                            break;
                        }
                    }
                }
            }
        }

        return removed > 0;
    }
    
    
    /**
     * Call this method before the page is going to be unloaded : triggers idevices
     * to save their values etc.
     */
    public void beforeUnload(HTMLComponent htmlC) {
        if(pageIdevices == null) {
            return;//first page has not yet actually loaded
        }
        
        Enumeration idevices = pageIdevices.elements();
        IdeviceJ2ME idevice;
        while(idevices.hasMoreElements()) {
            idevice = (IdeviceJ2ME)idevices.nextElement();
            idevice.beforeUnload(htmlC);
        }
    }

    public boolean linkClicked(HTMLComponent htmlC, String url) {
        parsingError(600, "a", "src", url, "link click");
        if(url.startsWith(UstadMobileSystemImplJ2ME.OPENZIP_PROTO)) {
            return true;
        }else {
            UstadMobileSystemImpl.getInstance().getAppView(this).showAlertDialog(
                "Link not supported", url);
        }
        
        return false;
    }

    
    
    public boolean parsingError(int errorId, String tag, String attribute, String value, String description) {
        UstadMobileSystemImpl.l(UMLog.ERROR, 300, "parsingError: id:" + errorId +
            "tag " + tag + " /attr: " + attribute + " /value: " + value +
            "/desc: " + description);
        
        
        return super.parsingError(errorId, tag, attribute, value, description); 
    }
    
    
    protected boolean findIdevices(HTMLComponent htmlC) {
        Vector ideviceEls = htmlC.getDOM().getDescendantsByClasses(IDEVICE_CSS_CLASSES, 
            IDEVICE_TAG_IDS);
        HTMLElement currentEl;
        Enumeration ideviceCSSClasses;
        String ideviceClassName;
        String pageTinCanID = getPageTinCanID(htmlC);
        boolean modified = false;
        
        boolean ideviceFound;
        for(int i = 0; i < ideviceEls.size(); i++) {
            currentEl = (HTMLElement)ideviceEls.elementAt(i);
            ideviceFound = false;
            ideviceCSSClasses = ContainerViewHTMLCallback.ideviceClasses.keys();
            while(ideviceCSSClasses.hasMoreElements() && !ideviceFound) {
                ideviceClassName = (String)ideviceCSSClasses.nextElement();
                if(currentEl.hasClass(ideviceClassName)) {
                    IdeviceJ2ME idevice = null;
                    try {
                        idevice = (IdeviceJ2ME)((Class)ideviceClasses.get(ideviceClassName)).newInstance();
                        idevice.setHtmlC(htmlC);
                        idevice.setHtmlCallback(this);
                        idevice.setIdeviceElement(currentEl);
                        idevice.setPageTinCanId(pageTinCanID);
                        idevice.setState(state);
                        idevice.setRegistrationUUID(registrationUUID);
                        modified = idevice.enhance() || modified;
                        pageIdevices.put(idevice.getId(), idevice);
                        ideviceFound = true;
                    }catch(Exception e) {
                        UstadMobileSystemImpl.l(UMLog.ERROR, i, 
                            currentEl.getAttributeById(HTMLElement.ATTR_ID), e);
                    }
                }
            }
        }
        
        return modified;
    }
    
    
    public void pageStatusChanged(HTMLComponent htmlC, int status, String url) {
        UstadMobileSystemImpl.l(UMLog.DEBUG, 604, url+':'+status);
        if (status == STATUS_REQUESTED) {
            fixedPage = false;
            domInvalid = false;
            if(pageIdevices == null) {
                pageIdevices= new Hashtable();
            }else {
                pageIdevices.clear();
            }
        }else if (status == STATUS_DISPLAYED && fixedPage == false) {
            if(view != null) {
                view.handlePageChange(url);
            }
            
            boolean modified = false;
            modified = hideExtras(htmlC) || modified;
            modified = findIdevices(htmlC);
            if (modified) {
                htmlC.refreshDOM();
            }

            fixedPage = true;
        }
        
        super.pageStatusChanged(htmlC, status, url);
    }

    public void dataChanged(int type, int index, HTMLComponent htmlC, TextField textField, HTMLElement element) {
        Enumeration idevices = pageIdevices.elements();
        IdeviceJ2ME idevice;
        while(idevices.hasMoreElements()) {
            idevice = (IdeviceJ2ME)idevices.nextElement();
            if(element.isDescendantOf(idevice.getIdeviceElement())) {
                idevice.dataChanged(index, index, htmlC, textField, element);
            }
        }
        
        super.dataChanged(type, index, htmlC, textField, element); 
    }

    public void actionPerformed(ActionEvent evt, HTMLComponent htmlC, HTMLElement element) {        
        Enumeration idevices = pageIdevices.elements();
        IdeviceJ2ME idevice;
        while(idevices.hasMoreElements()) {
            idevice = (IdeviceJ2ME)idevices.nextElement();
            if(element.isDescendantOf(idevice.getIdeviceElement())) {
                idevice.actionPerformed(evt, htmlC, element);
            }
        }
        
        if(domInvalid) {
            refreshDOM(htmlC);
        }

        super.actionPerformed(evt, htmlC, element); //To change body of generated methods, choose Tools | Templates.
    }

}
