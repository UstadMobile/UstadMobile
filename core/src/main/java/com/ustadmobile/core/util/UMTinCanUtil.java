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

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmAccountManager;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.tincan.TinCanStatement;
import com.ustadmobile.lib.db.entities.UmAccount;
import com.ustadmobile.lib.util.UMUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Vector;

/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
/* $endif$ */


/**
 * This class holds static utility functions to use with TinCan logic.
 * 
 * @author mike
 */
public class UMTinCanUtil {

    public static final String ADL_PREFIX_VERB = "http://adlnet.gov/expapi/verbs/";

    public static final String VERB_PASSED = ADL_PREFIX_VERB + "passed";

    public static final String VERB_FAILED = ADL_PREFIX_VERB + "failed";

    public static final String VERB_ANSWERED = ADL_PREFIX_VERB + "answered";


    /**
     * Generate a JSON Object representing a TinCan statement for 'experience' a 
     * given page.
     * 
     * Statement ID will be the EPUB ID/pageName
     * 
     * @param pageTitle Title of the page
     * @param pageLang language of the page for tincan purposes (e.g. en-US)
     * @param duration Duration viewed in ms
     * @param actor TinCan actor JSONObject
     * 
     * @return JSONObject representing the TinCan stmt, null if error
     */
    public static JSONObject makePageViewStmt(String tinCanID, String pageTitle, String pageLang, long duration, JSONObject actor) {
        JSONObject stmtObject = new JSONObject();
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
            UstadMobileSystemImpl.l(UMLog.ERROR, 199, tinCanID, e);
        }
        
        return stmtObject;
    }
    
    /**
     * Generate a JSONObject representing an Activity which is simply referenced
     * by it's ID.  Using just the ID is a good idea when the activity is already
     * known on the server end anyway.
     * 
     * @param id ID of the Activity object
     * @return JSONObject representing the Activity
     */
    public static JSONObject makeActivityObjectById(String id) {
        JSONObject obj = new JSONObject();
        Object definitionVal = null;
        try {
            obj.put("id", id);
            obj.put("objectType", "Activity");
            obj.put("definition", definitionVal);
        }catch(JSONException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 195, null, e);
        }
        
        return obj;
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
    
    /**
     * Make an actor JSON as per makeActorFromuserAccount for the currently
     * logged in user against the server that they logged in using
     * 
     * @see UMTinCanUtil#makeActorFromUserAccount(java.lang.String, java.lang.String) 
     * @param context Current context object
     * @return JSON Object representing the currently logged in user.
     */
    public static JSONObject makeActorFromActiveUser(Object context) {
        UmAccount account = UmAccountManager.getActiveAccount(context);
        return UMTinCanUtil.makeActorFromUserAccount(account.getUsername(),
                account.getEndpointUrl());
    }



    
    /**
     * Make a JSON object representing the verb in the form of:
     * {
     *  id : (id)
     *  display: {
     *    "(lang)" : "(display)"
     *  }
     * }
     * 
     * @param id ID of the verb e.g. http://adlnet.gov/expapi/verbs/answered
     * @param lang lang for display value: e.g. en-US
     * @param display text to represent verb in that language : e.g. answered
     * @return 
     */
    public static JSONObject makeVerbObject(String id, String lang, String display) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("display", makeLangMapVal(lang, display));
        }catch(JSONException je) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 195, "UMTinCanUtil.makeVerbObject", je);
        }
        
        return obj;
    }

    /**
     * 
     */
    public static String generateUUID() {
        return UMUUID.randomUUID().toString();
    }
    
    /**
     * Given a string that represents a statement result from the GET api
     * of a TinCan endpoint this method will convert them into an array
     * of JSON Objects each representing the individual statements themselves.
     * 
     * @param jsonStr The JSON returned by the server as a string
     * @return 
     */
    public static TinCanStatement[] getStatementsFromResult(String jsonStr) {
        TinCanStatement[] result = null;
        try {
            JSONObject resultObj = new JSONObject(jsonStr);
            if(resultObj.has("statements")) {
                //this is a StatementResult object
                JSONArray stmtArray = resultObj.getJSONArray("statements");
                result = new TinCanStatement[stmtArray.length()];
                for(int i = 0; i < result.length; i++) {
                    result[i] = new TinCanStatement(stmtArray.getJSONObject(i));
                }
            }else {
                //this is an individual statement
                result = new TinCanStatement[] { new TinCanStatement(resultObj) };
            }
        }catch(JSONException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 192, jsonStr, e);
        }
        
        return result;
    }
    
    
    /**
     * Go through a given list of statements and provide a String array of
     * all the distinct registration IDs found 
     * 
     * @param stmts Array of JSON objects, each should be an XAPI Statement
     * @return A String array of registration UUIDs found, 0 length String array if none found.  Not necssarily in the same order.
     */
    public static String[] getDistinctRegistrations(JSONObject[] stmts) {
        Vector resultVector = new Vector();
        String registration;
        for(int i = 0; i < stmts.length; i++) {
            registration = getStatementRegistration(stmts[i]);
            
            if(registration != null && resultVector.contains(registration)) {
                resultVector.addElement(registration);
            }   
        }
        
        String[] result = new String[resultVector.size()];
        resultVector.copyInto(result);
        return result;
    }
    
    /**
     * Gets the registration from a JSONObject representing an XAPI statement
     * if it has one
     * 
     * @param stmt JSONObject that represents a TinCan statement
     * @return The registration UUID if present in the context of statement, null otherwise
     */
    public static String getStatementRegistration(JSONObject stmt) {
        try {
            if(stmt.has("context")) {
                JSONObject context = stmt.getJSONObject("context");
                if(context.has("registration")) {
                    return context.getString("registration");
                }
            }
        }catch(JSONException e) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 194, null, e);
        }
        
        return null;
    }
    
    /**
     * Filters a list of registrations 
     * 
     * @param registrations List of registrations to filter 
     * @param keep If true all matching registrations are including, otherwise they are excluded
     * @param srcArr Array of registrations to go through
     * 
     * @return JSONObject array representing statements filtered according to params
     */
    public static JSONObject[] filterByRegistration(String[] registrations, boolean keep, JSONObject[] srcArr) {
        Vector results = new Vector();
        String stmtRegistration;
        int j;
        for(int i = 0; i < srcArr.length; i++) {
            stmtRegistration = getStatementRegistration(srcArr[i]);
            boolean match = false;
            if(stmtRegistration != null) {
                for(j = 0; j < registrations.length && !match; j++) {
                    match = registrations[i].equals(stmtRegistration);
                }
            }
            
            if(match && keep) {
                results.addElement(srcArr[i]);
            }else if(!match && !keep) {
                results.addElement(srcArr[i]);
            }
            
        }
        
        JSONObject[] resultStmts = new JSONObject[results.size()];
        results.copyInto(resultStmts);
        return resultStmts;
    }
    

    
    
}
