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
package com.ustadmobile.test.port.j2me;

import com.sun.lwuit.Form;
import com.sun.lwuit.html.DocumentInfo;
import com.sun.lwuit.html.DocumentRequestHandler;
import com.sun.lwuit.html.HTMLCallback;
import com.sun.lwuit.html.HTMLComponent;
import com.sun.lwuit.html.HTMLElement;
import com.sun.lwuit.layouts.BorderLayout;
import com.ustadmobile.core.impl.HTTPResult;
import com.ustadmobile.test.core.TestUtils;
import com.ustadmobile.core.impl.UstadMobileDefaults;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.port.j2me.view.ContainerViewHTMLCallback;
import com.ustadmobile.port.j2me.view.idevice.EXEQuizAnswer;
import com.ustadmobile.port.j2me.view.idevice.EXEQuizIdevice;
import com.ustadmobile.port.j2me.view.idevice.EXEQuizQuestion;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;



/**
 * Basic test of functionality that dynamically adapts eXeLearning generated quizzes
 * for use in the LWUIT HTML Component
 * 
 * @author mike
 */
public class TestJ2MEEXEMCQ extends TestCase {

    static final int TIMEOUT = 15000;
    static final int CHECKINTERVAL = 1000;
    
    
    protected void runTest() throws Throwable {
        Form testForm = new Form();
        HTMLComponent htmlC = new HTMLComponent(new TestDocumentRequestHandler());
        htmlC.setIgnoreCSS(true);
        htmlC.setEventsEnabled(true);
        testForm.setLayout(new BorderLayout());
        testForm.addComponent(BorderLayout.CENTER, htmlC);
        testForm.show();
        
        ContainerViewHTMLCallback callback = new ContainerViewHTMLCallback(
            UstadMobileDefaults.DEFAULT_TINCAN_PREFIX + "/exe-quiz-unittest", htmlC);
        htmlC.setHTMLCallback(callback);
        String testHTMLURL = UMFileUtil.joinPaths(new String[] {
            TestUtils.getInstance().getHTTPRoot(), "exe-mcq.xhtml"});
        htmlC.setPage(testHTMLURL);
        
        int timeRemaining = TIMEOUT;
        do {
            try { Thread.sleep(CHECKINTERVAL); }
            catch(InterruptedException e) {}
            timeRemaining -= CHECKINTERVAL;
        }while(htmlC.getPageStatus() != HTMLCallback.STATUS_COMPLETED && timeRemaining > 0);
        
        assertEquals("Page status is ready", HTMLCallback.STATUS_COMPLETED,
            htmlC.getPageStatus());
        
        Hashtable exeQuizzes = callback.getPageIdevices();
        assertTrue("Container view has found quizzes", exeQuizzes != null);
        String ideviceID = (String)exeQuizzes.keys().nextElement();
        EXEQuizIdevice idevice = (EXEQuizIdevice)exeQuizzes.get(ideviceID);
        assertNotNull("Can get first idevice", idevice);
        Vector questions = idevice.getQuestions();
        assertNotNull("idevice questions not null", questions);
        assertTrue("Idevice has questions", questions.size() > 0);
        EXEQuizQuestion question = (EXEQuizQuestion)questions.elementAt(0);
        assertNotNull("question has HTMLElement", question.getQuestionElement());
        Vector answers = question.getAnswers();
        assertTrue("Answers not null and size greater than 0",
            answers != null && answers.size() > 0);
        EXEQuizAnswer answer = (EXEQuizAnswer)answers.elementAt(0);
        HTMLElement answerFeedbackEl = answer.getFeedbackElement();
        assertTrue("answer has feedback element ", answerFeedbackEl != null);
        assertTrue("answer has input radio button element", answer.getInputElement() != null);
    }
        
    /**
     * Handles requests to load resources by using ustadmobilesystemimpl to make
     * an HTTP request
     */
    public class TestDocumentRequestHandler implements DocumentRequestHandler {        
        
        public TestDocumentRequestHandler() {
            
        }
        
        public InputStream resourceRequested(DocumentInfo di) {
            try {
                String url = di.getUrl();
                System.out.println("get " + url);
                HTTPResult result = UstadMobileSystemImpl.getInstance().makeRequest(di.getUrl(), null, null);
                return new ByteArrayInputStream(result.getResponse());
            }catch(IOException e) {
                return new ByteArrayInputStream("ERROR".getBytes());
            }
        }
        
    }
    
    
}
