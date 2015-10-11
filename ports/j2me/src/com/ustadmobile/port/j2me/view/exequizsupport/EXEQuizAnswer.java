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
package com.ustadmobile.port.j2me.view.exequizsupport;

import com.sun.lwuit.html.HTMLCallback;
import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.html.HTMLElement;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import java.util.Vector;


/**
 * Represents a possible answer for an EXEQuizQuestion
 * 
 * @author mike
 */
public class EXEQuizAnswer {
    
    private int answerIndex;
    
    private EXEQuizQuestion question;
    
    private HTMLElement answerElement;
    
    private HTMLElement answerContentElement;
    
    private HTMLElement inputElement;
        
    private HTMLElement feedbackElement;
    
    private HTMLElement feedbackElementParent;
    
    private HTMLComponent htmlC;
    
    private int feedbackElPosition;
    
    private boolean feedbackShowing = true;
    
    /**
     * Creates a new quiz answer
     * 
     * @param answerIndex Index of this answer e.g. 0 = first possible answer etc
     * @param question The question that this is a possible answer for
     * @param answerEl the HTMLElement containing the answer itself (e.g. text and radio button)
     * @param formEl The form element containing the entire question form (used to find the feedback element)
     */
    public EXEQuizAnswer(int answerIndex, EXEQuizQuestion question, HTMLElement answerEl, HTMLElement formEl, HTMLComponent htmlC) {
        this.answerIndex = answerIndex;
        this.question = question;
        this.answerElement = answerEl;
        this.htmlC = htmlC;
        setupFromElement(answerEl, formEl);
    }
    
    private void setupFromElement(HTMLElement answerEl, HTMLElement formEl) {
        inputElement = (HTMLElement)answerEl.getDescendantsByTagId(
                HTMLElement.TAG_INPUT).elementAt(0);
        String feedbackElId = "sa" + this.answerIndex + "b" + question.getID();
        setFeedbackElement((HTMLElement)formEl.getElementById(feedbackElId));
        
        /* 
         The input element has an id in the form of id='iXX_YY where XX_YY 
         matches up with a div which will have an id of answer-XX_YY
         */
        String answerID = inputElement.getAttributeById(HTMLElement.ATTR_ID).substring(1);
        answerContentElement = (HTMLElement)answerEl.getElementById("answer-" 
            + answerID);
        hideFeedback();
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
    public void showFeedback() {
        if(!feedbackShowing && feedbackElement != null) {
            feedbackElementParent.addChild(feedbackElement);
            feedbackShowing = true;
            
            //see if ther is a sound file here
            Vector audioTags = feedbackElement.getDescendantsByTagId(HTMLElement.TAG_AUDIO);
            UstadMobileSystemImpl.l(UMLog.DEBUG, 596, ""+audioTags.size());
            if(audioTags != null && audioTags.size() > 0 && htmlC.getHTMLCallback() != null) {
                HTMLCallback htmlCB = htmlC.getHTMLCallback();
                HTMLElement audioEl = (HTMLElement)audioTags.elementAt(0);
                htmlCB.mediaPlayRequested(HTMLCallback.MEDIA_AUDIO, HTMLCallback.MEDIA_PLAY, 
                    htmlC, null, audioEl);
            }
        }
    }
    
}
