/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.port.j2me.view.idevice;

import com.sun.lwuit.Component;
import com.sun.lwuit.List;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.html.HTMLCallback;
import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.html.HTMLElement;
import com.ustadmobile.core.controller.ContainerController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.port.j2me.view.ContainerViewHTMLCallback;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 *
 * Represents an Idevice element in HTML - anything where we are doing something
 * special with the HTML that would be handled on other devices using Javascript
 * 
 * 
 * 
 * @author mike
 */
public class IdeviceJ2ME implements HTMLCallback {
    
    protected HTMLElement ideviceEl;
    
    protected HTMLComponent htmlC;
    
    protected Object context;
    
    protected String pageTinCanId;
    
    protected JSONObject state;
    
    protected String registrationUUID;
    
    protected ContainerViewHTMLCallback htmlCallback;
    
    public IdeviceJ2ME() {
        //blank constructor as this is created by class.newInstance
    }
    
    /**
     * This method should perform any enhancements (e.g. add/remove HTML elements)
     * etc.
     * 
     * @return True if the DOM was modified (thus requiring a refresh) ; false otherwise
     */
    public boolean enhance() {
        return false;
    }
    
    public void setIdeviceElement(HTMLElement ideviceEl) {
        this.ideviceEl = ideviceEl;
    }
    
    /**
     * Provides the HTMLElement that actually holds the idevice itself - that is the one
     * with the CSS class idevice
     * 
     * @return 
     */
    public HTMLElement getIdeviceElement() {
        return ideviceEl;
    }

    public HTMLComponent getHtmlC() {
        return htmlC;
    }

    public void setHtmlC(HTMLComponent htmlC) {
        this.htmlC = htmlC;
    }

    public Object getContext() {
        return context;
    }

    public void setContext(Object context) {
        this.context = context;
    }

    public String getPageTinCanId() {
        return pageTinCanId;
    }

    public void setPageTinCanId(String pageTinCanId) {
        this.pageTinCanId = pageTinCanId;
    }

    public String getRegistrationUUID() {
        return registrationUUID;
    }

    public void setRegistrationUUID(String registrationUUID) {
        this.registrationUUID = registrationUUID;
    }

    public ContainerViewHTMLCallback getHtmlCallback() {
        return htmlCallback;
    }

    public void setHtmlCallback(ContainerViewHTMLCallback htmlCallback) {
        this.htmlCallback = htmlCallback;
    }
    
    /**
     * Return the idevice id: optionally including the "id" prefix added by eXeLearning
     * 
     * @param incPrefix - If true return with id prefix (e.g. idXX), otherwise return only XX
     * 
     * @return Idevice id as above
     */
    public String getId(boolean incPrefix) {
        if(incPrefix) {
            return ideviceEl.getAttributeById(HTMLElement.ATTR_ID);
        }else {
            return ideviceEl.getAttributeById(HTMLElement.ATTR_ID).substring(2);
        }
    }
    
    /**
     * Return the idevice ID including the "id" prefix
     * 
     * @return Idevice id including the prefix - synonymous with getId(true)
     */
    public String getId() {
        return getId(true);
    }
    
    public void setState(JSONObject state) {
        this.state = state;
    }
    
    public JSONObject getState() {
        return state;
    }
    
    /**
     * This method will get called by ContainerViewHTMLCallback just before the user
     * leaves the page
     */
    public void beforeUnload(HTMLComponent htmlC) {
        
    }
    

    public void titleUpdated(HTMLComponent htmlc, String string) {
        
    }

    public void pageStatusChanged(HTMLComponent htmlc, int i, String string) {
        
    }

    public String fieldSubmitted(HTMLComponent htmlc, TextArea ta, String string, String string1, String string2, int i, String string3) {
        return null;
    }

    public String getAutoComplete(HTMLComponent htmlc, String string, String string1) {
        return null;
    }

    public int getLinkProperties(HTMLComponent htmlc, String string) {
        return 0;
    }

    public boolean linkClicked(HTMLComponent htmlc, String string) {
        return false;
    }

    public void actionPerformed(ActionEvent ae, HTMLComponent htmlc, HTMLElement htmle) {
        
    }

    public void focusGained(Component cmpnt, HTMLComponent htmlc, HTMLElement htmle) {
        
    }

    public void focusLost(Component cmpnt, HTMLComponent htmlc, HTMLElement htmle) {
        
    }

    public void selectionChanged(int i, int i1, HTMLComponent htmlc, List list, HTMLElement htmle) {
        
    }

    public void dataChanged(int i, int i1, HTMLComponent htmlc, TextField tf, HTMLElement htmle) {
        
    }

    public void mediaPlayRequested(int i, int i1, HTMLComponent htmlc, String string, HTMLElement htmle) {
        
    }

    public boolean parsingError(int i, String string, String string1, String string2, String string3) {
        return false;
    }
    
}
