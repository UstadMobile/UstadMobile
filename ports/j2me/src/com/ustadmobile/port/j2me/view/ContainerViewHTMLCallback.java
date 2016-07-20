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
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMIOUtils;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.port.j2me.impl.UstadMobileSystemImplJ2ME;
import com.ustadmobile.port.j2me.view.exequizsupport.EXEQuizIdevice;
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

    private Hashtable mcqQuizzes;
    
    boolean fixedPage = true;

    static Hashtable mediaExtensions;
    
    String containerTinCanID;
    
    Object context;
    
    private String registrationUUID;
    
    private JSONObject state;
    
    public static final String IDEVICE_CLASS_TEXTENTRY = "TextEntryIdevice";
    
    public static final String TEXTENTRY_INPUTEL_PREFIX = "exe_tei_textel_";
    
        
    static {
        mediaExtensions = new Hashtable();
        mediaExtensions.put("mp3", "audio/mpeg");
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
     * Find eXeLearning generated MCQ questions and set them up so we can
     * dynamically show / hide the feedback for those answers
     *
     * @param htmlC HTML Componet in question
     * @return true if the DOM was modified - false otherwise
     */
    private boolean findEXEMCQs(HTMLComponent htmlC) {
        Vector quizElements = htmlC.getDOM().getDescendantsByClass("MultichoiceIdevice", 
                IDEVICE_TAG_IDS);   
        
        if(quizElements.size() == 0) {
            return false;
        }
        
        mcqQuizzes = new Hashtable();
        String pageTinCanID = getPageTinCanID(htmlC);
            
        HTMLElement quizEl;
        int numQuizzes = 0;
        for(int i = 0; i < quizElements.size(); i++) {
            quizEl = (HTMLElement)quizElements.elementAt(i);
            
            //There can be faulty empty idevices generated somehow... skip if this is what we found
            if(quizEl.getNumChildren() == 0) {
                continue;
            }
            
            EXEQuizIdevice quizDevice = new EXEQuizIdevice(
                    (HTMLElement)quizElements.elementAt(i), htmlC, context, 
                    pageTinCanID, i);
            quizDevice.setRegistrationUUID(registrationUUID);
            quizDevice.setState(state);
            mcqQuizzes.put(quizDevice.getID(), quizDevice);
            numQuizzes++;
        }
        
        return numQuizzes > 0;
    }
    
    public Hashtable getMCQQuizzes() {
        return mcqQuizzes;
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
    
    public void makeTextEntryStatements(HTMLComponent htmlC) {
        HTMLElement htmlEl = htmlC.getDOM();
        if(htmlEl == null)
            return;
        
        Vector textEntryInputEls = getTextEntryInputEls(htmlEl);
        if(textEntryInputEls == null)
            return;
        
        
        for(int i = 0; i < textEntryInputEls.size(); i++) {
            HTMLElement currentEl = (HTMLElement)textEntryInputEls.elementAt(i);
            String dirtyAttrVal = currentEl.getAttribute("data-dirty");
            if(dirtyAttrVal != null && state != null) {
                JSONObject stmt = new JSONObject();
                String ideviceId = "id" + currentEl.getAttributeById(
                    HTMLElement.ATTR_ID).substring(TEXTENTRY_INPUTEL_PREFIX.length());
                String responseVal = state.optString(ideviceId, null);
                try {
                    stmt.put("object", UMTinCanUtil.makeActivityObjectById(
                        getPageTinCanID(htmlC) + '/' + ideviceId));
                    stmt.put("actor", UMTinCanUtil.makeActorFromActiveUser(
                        context));
                    stmt.put("verb", UMTinCanUtil.makeVerbObject(
                        "http://adlnet.gov/expapi/verbs/answered", "en-US", "answered"));
                    JSONObject resultObj = new JSONObject();
                    resultObj.put("response", responseVal);
                    stmt.put("result", resultObj);
                    UstadMobileSystemImpl.getInstance().queueTinCanStatement(stmt, 
                        context);
                    currentEl.removeAttribute("data-dirty");
                }catch(JSONException e) {
                    UstadMobileSystemImpl.l(UMLog.ERROR, 195, ideviceId, e);
                }
            }
        }
    }
    
    private Vector getTextEntryInputEls(HTMLElement parentEl) {
        Vector returnVal = null;
        Vector textEntryIdevices = parentEl.getDescendantsByClass(
                IDEVICE_CLASS_TEXTENTRY, IDEVICE_TAG_IDS);
        Vector inputEls;
        HTMLElement ideviceEl;
        HTMLElement inputEl;
        String id;
        for(int i = 0; i < textEntryIdevices.size(); i++) {
            ideviceEl = (HTMLElement)textEntryIdevices.elementAt(i);
            inputEls = ideviceEl.getDescendantsByTagId(HTMLElement.TAG_INPUT);
            if(inputEls == null || inputEls.size() ==0) {
                inputEls = ideviceEl.getDescendantsByTagId(HTMLElement.TAG_TEXTAREA);
            }

            inputEl = (inputEls != null && inputEls.size() > 0) ? (HTMLElement)inputEls.elementAt(0) : null;
            if(inputEl != null) {
                id = inputEl.getAttributeById(HTMLElement.ATTR_ID);
                if(id != null && id.startsWith(TEXTENTRY_INPUTEL_PREFIX)) {
                    if(returnVal == null) {
                        returnVal = new Vector();
                    }
                    returnVal.addElement(inputEl);
                }
            }
        }

        return returnVal;
    }
    
    
    
    /**
     * Go through text entry idevices : if there are state values from previous
     * answers update the DOM so that these values will be shown.
     * 
     * @param htmlC HTMLComponent being operated on
     * @param state JSON state object
     * 
     * @return true if the DOM was modified; false otherwise
     */
    protected boolean setInputValuesFromState(HTMLComponent htmlC, JSONObject state) {
        boolean modified = false;
        if(state != null && htmlC.getDOM() != null) {
            Vector textEntryInputEls = getTextEntryInputEls(htmlC.getDOM());
            
            if(textEntryInputEls == null || textEntryInputEls.size() == 0) {
                return modified;
            }
            
            HTMLElement inputEl;
            String ideviceId, value;
            for(int i = 0; i < textEntryInputEls.size(); i++) {
                inputEl = (HTMLElement)textEntryInputEls.elementAt(i);
                ideviceId = "id" + inputEl.getAttributeById(HTMLElement.ATTR_ID).substring(
                    TEXTENTRY_INPUTEL_PREFIX.length());
                if(state.has(ideviceId)) {
                    value = state.optString(ideviceId, null);
                    switch(inputEl.getTagId()) {
                        case HTMLElement.TAG_INPUT:
                            inputEl.setAttributeById(HTMLElement.ATTR_VALUE, 
                                value);
                            break;
                        case HTMLElement.TAG_TEXTAREA:
                            int numChildren = inputEl.getNumChildren();
                            for(int j = numChildren-1; j >= 0; j--) {
                                inputEl.removeChildAt(j);
                            };
                            inputEl.addChild(new HTMLElement(value, true));
                            break;
                    }
                    modified = true;
                }
            }
        }
        
        return modified;
    }
    

    public void pageStatusChanged(HTMLComponent htmlC, int status, String url) {
        UstadMobileSystemImpl.l(UMLog.DEBUG, 604, url+':'+status);
        if (status == STATUS_REQUESTED) {
            fixedPage = false;
        }else if (status == STATUS_DISPLAYED && fixedPage == false) {
            if(view != null) {
                view.handlePageChange(url);
            }
            
            boolean modified = false;
            mcqQuizzes = new Hashtable();

            modified = findEXEMCQs(htmlC) || modified;
            modified = hideExtras(htmlC) || modified;
            modified = setInputValuesFromState(htmlC, state) || modified;
            
            Enumeration quizzesOnPage = mcqQuizzes.elements();
            EXEQuizIdevice currentQuiz;
            while(quizzesOnPage.hasMoreElements()) {
                currentQuiz = (EXEQuizIdevice)quizzesOnPage.nextElement();
                modified = currentQuiz.formatQuestionsAsTables() || modified;
            }
            currentQuiz = null;

            if (modified) {
                htmlC.refreshDOM();
            }

            fixedPage = true;
        }
        
        super.pageStatusChanged(htmlC, status, url);
    }

    public void dataChanged(int type, int index, HTMLComponent htmlC, TextField textField, HTMLElement element) {
        String elId = element.getAttributeById(HTMLElement.ATTR_ID);
        if(elId != null && elId.startsWith("exe_tei_") && state != null) {
            String ideviceId = "id" + elId.substring(elId.lastIndexOf('_')+1);
            try {
                state.put(ideviceId, textField.getText());
                element.setAttribute("data-dirty", "true");
            }catch(JSONException e) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 195, null, e);
            }
        }
        
        super.dataChanged(type, index, htmlC, textField, element); 
    }

    public void actionPerformed(ActionEvent evt, HTMLComponent htmlC, HTMLElement element) {
        boolean domChanged = false;
        if (element != null && element.getTagId() == HTMLElement.TAG_INPUT) {
            String inputType = element.getAttributeById(HTMLElement.ATTR_TYPE);
            if (inputType.equalsIgnoreCase("radio")) {
                String mcqName = element.getAttributeById(HTMLElement.ATTR_NAME);
                if (mcqName == null || !mcqName.startsWith("option")) {
                    return;//this is not an eXeLearning MCQ
                }

                //In eXeLearning the questionID comes immediately after the option in name
                //e.g. "option20_67" MCQ ID = 20_67
                String quizID = mcqName.substring(6, mcqName.indexOf('_', 6));
                
                if (mcqQuizzes.containsKey(quizID)) {
                    EXEQuizIdevice quizDevice = (EXEQuizIdevice)mcqQuizzes.get(quizID);
                    domChanged = quizDevice.handleSelectAnswer(element) || domChanged;
                }

            }
        }
        
        if(domChanged) {
            htmlC.refreshDOM();
        }

        super.actionPerformed(evt, htmlC, element); //To change body of generated methods, choose Tools | Templates.
    }

}
