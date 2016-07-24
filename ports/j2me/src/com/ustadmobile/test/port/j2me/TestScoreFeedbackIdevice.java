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

import com.ustadmobile.port.j2me.view.idevice.ScoreFeedbackIdevice;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 *
 * @author mike
 */
public class TestScoreFeedbackIdevice extends TestCase{
    
    private JSONObject makeScoreObj(double score) throws JSONException{
        JSONObject obj = new JSONObject();
        obj.put("score", score);
        return obj;
    }
    
    protected void runTest() throws Throwable{
        JSONObject stateVals = new JSONObject();
        
        stateVals.put("id0_1", makeScoreObj(1.1));
        stateVals.put("id0_2", makeScoreObj(1.0));
        stateVals.put("id1_1", makeScoreObj(1.0));
        stateVals.put("id1_2", makeScoreObj(1.2));
        
        assertTrue("Parse value from substring",
            ScoreFeedbackIdevice.evalValue(
            "eXeTinCan.getPkgStateScoreSync(\"epub:4f73f3b4-642f-49d4-b5ba-2a20d6d83159/Page_1.xhtml/0_1\")", stateVals) == 1.1);
        assertTrue("Parse value from substring",
            ScoreFeedbackIdevice.evalValue(
            "0.5", stateVals) == 0.5);
        
        
        assertTrue("Basic expr: 1.1 == 1.0", 
            ScoreFeedbackIdevice.evaluateExpr(
            "eXeTinCan.getPkgStateScoreSync(\"epub:4f73f3b4-642f-49d4-b5ba-2a20d6d83159/Page_1.xhtml/0_1\") == 1.1", stateVals));
        
        assertTrue("Basic expr: 1.1 != 0.1", 
            !ScoreFeedbackIdevice.evaluateExpr(
            "eXeTinCan.getPkgStateScoreSync(\"epub:4f73f3b4-642f-49d4-b5ba-2a20d6d83159/Page_1.xhtml/0_1\") == 0.1", stateVals));
        
        assertTrue("Basic expr: 1.1 > 1.0", 
            ScoreFeedbackIdevice.evaluateExpr(
            "eXeTinCan.getPkgStateScoreSync(\"epub:4f73f3b4-642f-49d4-b5ba-2a20d6d83159/Page_1.xhtml/0_1\") > 1.0", stateVals));
        
        assertTrue("Basic expr: 1.1 > 1.0", 
            ScoreFeedbackIdevice.evaluateExpr(
            "eXeTinCan.getPkgStateScoreSync(\"epub:4f73f3b4-642f-49d4-b5ba-2a20d6d83159/Page_1.xhtml/0_1\") > 1.0 && eXeTinCan.getPkgStateScoreSync(\"epub:4f73f3b4-642f-49d4-b5ba-2a20d6d83159/Page_1.xhtml/0_2\") == 1", stateVals));
        
    }
    
}
