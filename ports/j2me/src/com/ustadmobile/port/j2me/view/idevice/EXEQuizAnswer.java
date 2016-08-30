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
package com.ustadmobile.port.j2me.view.idevice;

import com.sun.lwuit.html.HTMLCallback;
import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.html.HTMLElement;
import com.ustadmobile.core.controller.ContainerController;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.util.UMTinCanUtil;
import com.ustadmobile.port.j2me.view.ContainerViewHTMLCallback;
import java.util.Vector;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;


/**
 * Represents a possible answer for an EXEQuizQuestion
 * 
 * @author mike
 */
public class EXEQuizAnswer implements PlayerListener{
    
    private int answerIndex;
    
    private EXEQuizQuestion question;
    
    protected HTMLElement answerElement;
    
    protected HTMLElement answerContentElement;
    
    private HTMLElement inputElement;
        
    private HTMLElement feedbackElement;
    
    private HTMLElement feedbackElementParent;
        
    private int feedbackElPosition;
    
    private boolean feedbackShowing = true;
    
    private String linkBranch;
    
    /**
     * Creates a new quiz answer
     * 
     * @param answerIndex Index of this answer e.g. 0 = first possible answer etc
     * @param question The question that this is a possible answer for
     * @param answerEl the HTMLElement containing the answer itself (e.g. text and radio button)
     * @param formEl The form element containing the entire question form (used to find the feedback element)
     */
    public EXEQuizAnswer(int answerIndex, EXEQuizQuestion question, HTMLElement answerEl, HTMLElement formEl) {
        this.answerIndex = answerIndex;
        this.question = question;
        this.answerElement = answerEl;
        setupFromElement(answerEl, formEl);
    }
    
    private void setupFromElement(HTMLElement answerEl, HTMLElement formEl) {
        inputElement = (HTMLElement)answerEl.getDescendantsByTagId(
                HTMLElement.TAG_INPUT).elementAt(0);
        String feedbackElId = "sa" + this.answerIndex + "b" + question.getID();
        HTMLElement feedbackEl = (HTMLElement)formEl.getElementById(feedbackElId);
        setFeedbackElement(feedbackEl);
        
        /* 
         The input element has an id in the form of id='iXX_YY where XX_YY 
         matches up with a div which will have an id of answer-XX_YY
         */
        //String answerID = inputElement.getAttributeById(HTMLElement.ATTR_ID).substring(1);
        answerContentElement = (HTMLElement)answerEl.getElementById("answer-" 
            + getID());
        hideFeedback();
    }
    
    public String getID() {
        /* 
         The input element has an id in the form of id='iXX_YY where XX_YY 
         matches up with a div which will have an id of answer-XX_YY
         */
        return inputElement.getAttributeById(HTMLElement.ATTR_ID).substring(1);
    }
        
    public int getAnswerIndex() {
        return answerIndex;
    }
    
    public EXEQuizQuestion getQuestion() {
        return question;
    }
    
    public HTMLElement getInputElement() {
        return inputElement;
    }
    
    /**
     * The feedback element that should be shown when this answer is selected
     * by the user
     * 
     * @param feedbackElement element to show when user selects this answer
     */
    public void setFeedbackElement(HTMLElement feedbackElement) {
        this.feedbackElement = feedbackElement;
    }
    
    /**
     * The feedback element that should be shown when this answer is selected
     * by the user
     * 
     * @return HTMLElement that is shown as feedback for selecting this answer
     */
    public HTMLElement getFeedbackElement() {
        return this.feedbackElement;
    }
    
    public HTMLElement getAnswerContentElement() {
        return this.answerContentElement;
    }
    
    /**
     * Hide the feedback for this answer.  This is done by removing the element
     * from the DOM ; we can put it back there later when we want this to be seen
     */
    public void hideFeedback() {
        if(feedbackShowing && feedbackElement != null) {
            feedbackElementParent = (HTMLElement)feedbackElement.getParent();
            feedbackElPosition = feedbackElementParent.getChildIndex(feedbackElement);
            feedbackElementParent.removeChildAt(feedbackElPosition);
            feedbackShowing = false;
        }
    }
    
    /**
     * Show the feedback for this answer: this is done only after hideFeedback
     * by putting the element back into it's original parent
     */
    public void showFeedback(HTMLComponent htmlC) {
        if(!feedbackShowing && feedbackElement != null) {
            feedbackElementParent.addChild(feedbackElement);
            feedbackShowing = true;
            
            //see if ther is a sound file here
            Vector audioTags = feedbackElement.getDescendantsByTagId(HTMLElement.TAG_AUDIO);
            String branchType = feedbackElement.getAttribute("data-branch-type");
            
            if(audioTags != null && audioTags.size() > 0 && question.iDevice.getHtmlCallback() != null) {
                ContainerViewHTMLCallback htmlCB = question.iDevice.getHtmlCallback();
                HTMLElement audioEl = (HTMLElement)audioTags.elementAt(0);
                PlayerListener pl = null;
                if(branchType != null && branchType.equals("aftermedia")) {
                    pl = this;
                }
                
                /* TODO: fix this to use the default playback system...
                htmlCB.mediaPlayRequested(HTMLCallback.MEDIA_AUDIO, HTMLCallback.MEDIA_PLAY, 
                    htmlC, null, audioEl, pl);
                */
                goRedirect(htmlC);
            }else if(branchType != null && branchType.equals("immediate")) {
                goRedirect(htmlC);
            }
        }
    }
    
    /**
     * Marks the given state object for this question id that this answer is the
     * selected answer
     * 
     * @param state 
     */
    public void setStateSelectedAnswer(JSONObject state) {
        if(state != null) {
            try {
                JSONObject stateObj = new JSONObject();
                stateObj.put("response", getID());
                float score = getScore();
                if(score != Float.NaN) {
                    stateObj.put("score", score);
                }
                
                state.put("id" + question.getID(), stateObj);
            }catch(JSONException e) {
                
            }
        }
    }
    
    /**
     * Get the score associated with this answer as a float - reflects the value
     * put in place by exe as data-score.  
     * 
     * If data-score is not present on the attribute or an exception is thrown
     * whilst parsing that value this method will return Float.NaN
     * 
     * @return Score as a float if data-score is present and valid, Float.NaN otherwise
     */
    public float getScore() {
        try {
            String scoreStr = answerContentElement.getAttribute("data-score");
            if(scoreStr != null) {
                return Float.parseFloat(scoreStr);
            }
        }catch(NumberFormatException n) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 184, null, n);
        }
        
        return Float.NaN;
    }
    
    
    /**
     * Make a statement for the selection of the given answer
     * 
     * TODO: Move section generating the tincan definition to the question
     * @return 
     */
    public JSONObject makeTinCanStmt() {
        JSONObject stmt = new JSONObject();
        try {
            stmt.put("object", question.getTinCanObject());
            stmt.put("actor", UMTinCanUtil.makeActorFromActiveUser(
                    question.iDevice.getContext()));
            stmt.put("verb", UMTinCanUtil.makeVerbObject(
                "http://adlnet.gov/expapi/verbs/answered", "en-US", "answered"));
            JSONObject resultObj = new JSONObject();
            resultObj.put("response", getID());
            
            float answerScore = getScore();
            if(answerScore != Float.NaN) {
                try {
                    JSONObject scoreObj = new JSONObject();
                    scoreObj.put("raw", answerScore);
                    resultObj.put("score", scoreObj);
                }catch(Exception e) {}
            }
            
            stmt.put("result", resultObj);
            
            JSONObject context;
            String registrationUUID = question.iDevice.getRegistrationUUID();
            if(registrationUUID != null) {
                context = ContainerController.makeTinCanContext(registrationUUID);
            }else {
                context = new JSONObject();
            }
            
            JSONObject contextActivities = new JSONObject();
            JSONArray parentArr = new JSONArray();
            JSONObject parentObj = new JSONObject();
            
            parentObj.put("id", question.iDevice.getPageTinCanId());
            parentArr.put(parentObj);
            contextActivities.put("parent", parentArr);
            context.put("contextActivities", contextActivities);
            
            stmt.put("context", context);
        }catch(JSONException je) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 195, "a");
        }
        
        return stmt;
    }
    
    /**
     * Send the associated HTML component to the redirect if there is a redirect
     * specified for this answer
     */
    protected void goRedirect(HTMLComponent htmlC) {
        String branchStr = feedbackElement.getAttribute("data-branch-href");
        if(branchStr != null){ 
            String newURL = UMFileUtil.resolveLink(htmlC.getPageURL(), 
                    branchStr);
            htmlC.setPage(newURL);
        }
    }

    /**
     * Handle play update event - this is used to watch for the end of media
     * after playing selected media - so we can run the redirect once the media
     * is finished
     * 
     * @param player
     * @param event
     * @param eventData 
     */
    public void playerUpdate(Player player, String event, Object eventData) {
        if(event.equals(PlayerListener.END_OF_MEDIA)) {
            //redirect time - disable temporarily
            //goRedirect();
        }
    }
    
    
}
