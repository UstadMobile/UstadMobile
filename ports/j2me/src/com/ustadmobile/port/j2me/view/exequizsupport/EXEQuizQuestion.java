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

import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.html.HTMLElement;
import java.util.Vector;

/**
 *
 * This class represents a Quiz Question that was made in eXeLearning and
 * handles hiding and showing the feedback for each answer
 * 
 * @author mike
 */
public class EXEQuizQuestion {
    
    private String id;
    
    private Vector answers;
    
    private HTMLComponent htmlC;
    
    /**
     * Makes a new QuizQuestion element 
     * 
     * @param id The Question id - normally in the form of ideviceid_blockid eg. 20_69
     * @param htmlC HTML Component the question is contained within
     */
    public EXEQuizQuestion(String id, HTMLComponent htmlC) {
        this.id = id;
        this.htmlC = htmlC;
        this.answers = new Vector();
    }
    
    /**
     * Add a possible answer for this question
     * 
     * @param answer 
     */
    public void addAnswer(EXEQuizAnswer answer) {
        answers.addElement(answer);
    }
    
    /**
     * Find the answer represented by an input element (eg. the radio button)
     * 
     * @param inputEl The input Element from the document
     * @return The answer elment that this input element represents or null if none
     */
    public EXEQuizAnswer getAnswerByInputElement(HTMLElement inputEl) {
        EXEQuizAnswer curAnswer;
        for(int i = 0; i < answers.size(); i++) {
            curAnswer = (EXEQuizAnswer)answers.elementAt(i);
            if(curAnswer.getInputElement() == inputEl) {
                return curAnswer;
            }
        }
        
        return null;
    }
    
    /**
     * Show the MCQ feedback - show the fedback for the selected answer and hide
     * all others
     * 
     * @param selectedAnswer 
     */
    public void showMCQFeedback(EXEQuizAnswer selectedAnswer) {
        EXEQuizAnswer curAnswer;
        for(int i = 0; i < answers.size(); i++) {
            curAnswer = (EXEQuizAnswer)answers.elementAt(i);
            if(curAnswer == selectedAnswer) {
                curAnswer.showFeedback();
            }else {
                curAnswer.hideFeedback();
            }
        }
        htmlC.refreshDOM();
        HTMLElement feedbackEl = selectedAnswer.getFeedbackElement();
        
        htmlC.scrollToElement(feedbackEl, true);
    }
    
    
}
