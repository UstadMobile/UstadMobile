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
package com.ustadmobile.core.util;

import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;

/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
    import org.json.*;
/* $endif$ */


/**
 * This class holds static utility functions to use with TinCan logic.
 * 
 * @author mike
 */
public class UMTinCanUtil {
    
    /**
     * Generate a JSON Object representing a TinCan statement for 'experience' a 
     * given page.
     * 
     * Statement ID will be the EPUB ID/pageName
     * 
     * @param tcParent TinCan ID URL prefix e.g. http://www.ustadmobile.com/xapi/pkgid
     * @param pageName Name of the page - e.g. intro (without .xml etc)
     * @param pageTitle Title of the page
     * @param pageLang language of the page for tincan purposes (e.g. en-US)
     * @param duration Duration viewed in ms
     * @param actor TinCan actor JSONObject
     * 
     * @return JSONObject representing the TinCan stmt, null if error
     */
    public static JSONObject makePageViewStmt(String tcParent, String pageName, String pageTitle, String pageLang, long duration, JSONObject actor) {
        JSONObject stmtObject = new JSONObject();
        String tinCanID = tcParent + "/" + pageName;
        try {
            stmtObject.put("actor", actor);
            
            JSONObject activityDef = new JSONObject();
            activityDef.put("type", "http://adlnet.gov/expapi/activities/module");
            activityDef.put("name", makeLangMapVal(pageLang, pageTitle));
            activityDef.put("description", makeLangMapVal(pageLang, pageTitle));
            
            JSONObject objectDef = new JSONObject();
            objectDef.put("id", tinCanID);
            objectDef.put("definition", activityDef);
            objectDef.put("objectType", "Activity");
            stmtObject.put("object", objectDef);

            
            JSONObject verbDef = new JSONObject();
            verbDef.put("id", "http://adlnet.gov/expapi/verbs/experienced");
            JSONObject verbDisplay = new JSONObject();
            verbDisplay.put("en-US", "experienced");
            verbDef.put("display", verbDisplay);
            stmtObject.put("verb", verbDef);
            
            String stmtDuration = format8601Duration(duration);
            JSONObject resultDef = new JSONObject();
            resultDef.put("duration", stmtDuration);
            stmtObject.put("result", resultDef);
            
            //Uncomment if required for debugging
            //String totalStmtStr = stmtObject.toString();
        }catch(JSONException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 199, tcParent + '/' + pageName, 
                e);
        }
        
        return stmtObject;
    }
    
    /**
     * TinCan generally wants values to be read as a language map e.g.
     * { 'en-US' : 'Cant spell color' } .  In the case of content in a given
     * language there is really only ever one value
     * 
     * @param lang Language the string is in
     * @param langValue Value of the string in that language
     * @return JSONObject with key lang and value of langValue
     */
    public static JSONObject makeLangMapVal(String lang, String langValue) {
        JSONObject retVal = new JSONObject();
        try {
            retVal.put(lang, langValue);
        }catch(JSONException e) {
            //this should never happen - all we did is put two strings in a JSON object
            UstadMobileSystemImpl.l(UMLog.ERROR, 197, lang + '/' + langValue, 
                e);
        }
        
        return retVal;
    }
    
    /**
     * Format an ISO 8601 Duration from the number of milliseconds
     * 
     * @param duration Duration time in MS
     * 
     * @return A string formatted according to ISO8601 Duration e.g. P2H1M15S
     */
    public static String format8601Duration(long duration) {
        int msPerHour = (1000*60*60);
        int hours = (int)Math.floor(duration/msPerHour);
        long durationRemaining = duration % msPerHour;
        
        int msPerMin = (60*1000);
        int mins = (int)Math.floor(durationRemaining/msPerMin);
        durationRemaining = durationRemaining % msPerMin;
        
        int msPerS = 1000;
        int secs = (int)Math.floor(durationRemaining / msPerS);
        
        String retVal = "PT" + hours +"H" + mins + "M" + secs + "S";
        return retVal;
    }
    
    /**
     * Makes an actor JSON in the form of 
     * {
     *  account: {
     *  homePage: "http://twitter.com",
     *  name: "projecttincan"
     *  } ,
     *  objectType: "Agent"
     * }
     * 
     * See: https://tincanapi.com/deep-dive-actor-agent/
     * 
     * @param username The user id used for authentication on the server
     * @param xAPIServer The XAPI server to use for the homePage.  Should be the base e.g. server.com/xapi not server.com/xapi/statements
     * 
     */
    public static JSONObject makeActorFromUserAccount(String username, String xAPIServer) {
        JSONObject actorObj = null;
        try {
            actorObj = new JSONObject();
            JSONObject accountObj = new JSONObject();
            accountObj.put("homePage", xAPIServer);
            accountObj.put("name", username);
            actorObj.put("account", accountObj);
            actorObj.put("objectType", "Agent");
        }catch(JSONException e) {
            //seriously... this should never happen putting strings together
            UstadMobileSystemImpl.l(UMLog.ERROR, 195, null, e);
        }
        
        return actorObj;
    }
}
