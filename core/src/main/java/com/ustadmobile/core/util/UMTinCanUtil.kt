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
package com.ustadmobile.core.util

import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.tincan.TinCanStatement
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import javax.xml.datatype.DatatypeConfigurationException
import javax.xml.datatype.DatatypeFactory

/* $if umplatform == 2  $
    import org.json.me.*;
 $else$ */
/* $endif$ */


/**
 * This class holds static utility functions to use with TinCan logic.
 *
 * @author mike
 */
object UMTinCanUtil {

    val ADL_PREFIX_VERB = "http://adlnet.gov/expapi/verbs/"

    val VERB_PASSED = ADL_PREFIX_VERB + "passed"

    val VERB_FAILED = ADL_PREFIX_VERB + "failed"

    val VERB_ANSWERED = ADL_PREFIX_VERB + "answered"


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
    fun makePageViewStmt(tinCanID: String, pageTitle: String, pageLang: String, duration: Long, actor: JSONObject): JSONObject {
        val stmtObject = JSONObject()
        try {
            stmtObject.put("actor", actor)

            val activityDef = JSONObject()
            activityDef.put("type", "http://adlnet.gov/expapi/activities/module")
            activityDef.put("name", makeLangMapVal(pageLang, pageTitle))
            activityDef.put("description", makeLangMapVal(pageLang, pageTitle))

            val objectDef = JSONObject()
            objectDef.put("id", tinCanID)
            objectDef.put("definition", activityDef)
            objectDef.put("objectType", "Activity")
            stmtObject.put("object", objectDef)


            val verbDef = JSONObject()
            verbDef.put("id", "http://adlnet.gov/expapi/verbs/experienced")
            val verbDisplay = JSONObject()
            verbDisplay.put("en-US", "experienced")
            verbDef.put("display", verbDisplay)
            stmtObject.put("verb", verbDef)

            val stmtDuration = format8601Duration(duration)
            val resultDef = JSONObject()
            resultDef.put("duration", stmtDuration)
            stmtObject.put("result", resultDef)

            //Uncomment if required for debugging
            //String totalStmtStr = stmtObject.toString();
        } catch (e: JSONException) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 199, tinCanID, e)
        }

        return stmtObject
    }

    /**
     * Generate a JSONObject representing an Activity which is simply referenced
     * by it's ID.  Using just the ID is a good idea when the activity is already
     * known on the server end anyway.
     *
     * @param id ID of the Activity object
     * @return JSONObject representing the Activity
     */
    fun makeActivityObjectById(id: String): JSONObject {
        val obj = JSONObject()
        val definitionVal: Any? = null
        try {
            obj.put("id", id)
            obj.put("objectType", "Activity")
            obj.put("definition", definitionVal)
        } catch (e: JSONException) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 195, null, e)
        }

        return obj
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
    fun makeLangMapVal(lang: String, langValue: String): JSONObject {
        val retVal = JSONObject()
        try {
            retVal.put(lang, langValue)
        } catch (e: JSONException) {
            //this should never happen - all we did is put two strings in a JSON object
            UstadMobileSystemImpl.l(UMLog.ERROR, 197, "$lang/$langValue",
                    e)
        }

        return retVal
    }

    /**
     * Format an ISO 8601 Duration from the number of milliseconds
     *
     * @param duration Duration time in MS
     *
     * @return A string formatted according to ISO8601 Duration e.g. P2H1M15S
     */
    fun format8601Duration(duration: Long): String {
        val msPerHour = 1000 * 60 * 60
        val hours = Math.floor((duration / msPerHour).toDouble()).toInt()
        var durationRemaining = duration % msPerHour

        val msPerMin = 60 * 1000
        val mins = Math.floor((durationRemaining / msPerMin).toDouble()).toInt()
        durationRemaining = durationRemaining % msPerMin

        val msPerS = 1000
        val secs = Math.floor((durationRemaining / msPerS).toDouble()).toInt()

        return "PT" + hours + "H" + mins + "M" + secs + "S"
    }


    fun parse8601Duration(duration: String): Long {

        try {
            val time = DatatypeFactory.newInstance().newDuration(duration)
            return time.getTimeInMillis(Calendar.getInstance())
        } catch (e: Exception) {
            when (e) {
                is DatatypeConfigurationException, is IllegalArgumentException -> {
                    try {
                        if (duration.contains("S")) {
                            val seconds = duration.substring(duration.indexOf("P") + 1, duration.indexOf("S")).toLong()
                            return seconds * 1000
                        } else if (duration.contains("W")) {
                            val weeks = duration.substring(duration.indexOf("P") + 1, duration.indexOf("W")).toLong()
                            return weeks * 604800000
                        }
                    } catch (ignored: NumberFormatException) {
                    }
                }
            }
        }

        return 0
    }

    /**
     * Makes an actor JSON in the form of
     * {
     * account: {
     * homePage: "http://twitter.com",
     * name: "projecttincan"
     * } ,
     * objectType: "Agent"
     * }
     *
     * See: https://tincanapi.com/deep-dive-actor-agent/
     *
     * @param username The user id used for authentication on the server
     * @param xAPIServer The XAPI server to use for the homePage.  Should be the base e.g. server.com/xapi not server.com/xapi/statements
     */
    fun makeActorFromUserAccount(username: String, xAPIServer: String): JSONObject? {
        var actorObj: JSONObject? = null
        try {
            actorObj = JSONObject()
            val accountObj = JSONObject()
            accountObj.put("homePage", xAPIServer)
            accountObj.put("name", username)
            actorObj.put("account", accountObj)
            actorObj.put("objectType", "Agent")
        } catch (e: JSONException) {
            //seriously... this should never happen putting strings together
            UstadMobileSystemImpl.l(UMLog.ERROR, 195, null, e)
        }

        return actorObj
    }

    /**
     * Make an actor JSON as per makeActorFromuserAccount for the currently
     * logged in user against the server that they logged in using
     *
     * @see UMTinCanUtil.makeActorFromUserAccount
     * @param context Current context object
     * @return JSON Object representing the currently logged in user.
     */
    fun makeActorFromActiveUser(context: Any): JSONObject? {
        val account = UmAccountManager.getActiveAccount(context)
        return if (account != null) {
            UMTinCanUtil.makeActorFromUserAccount(account.username,
                    account.endpointUrl)
        } else {
            UMTinCanUtil.makeActorFromUserAccount("anonymous",
                    UmAccountManager.getActiveEndpoint(context)!!)
        }

    }


    /**
     * Make a JSON object representing the verb in the form of:
     * {
     * id : (id)
     * display: {
     * "(lang)" : "(display)"
     * }
     * }
     *
     * @param id ID of the verb e.g. http://adlnet.gov/expapi/verbs/answered
     * @param lang lang for display value: e.g. en-US
     * @param display text to represent verb in that language : e.g. answered
     * @return
     */
    fun makeVerbObject(id: String, lang: String, display: String): JSONObject {
        val obj = JSONObject()
        try {
            obj.put("id", id)
            obj.put("display", makeLangMapVal(lang, display))
        } catch (je: JSONException) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 195, "UMTinCanUtil.makeVerbObject", je)
        }

        return obj
    }

    /**
     * Given a string that represents a statement result from the GET api
     * of a TinCan endpoint this method will convert them into an array
     * of JSON Objects each representing the individual statements themselves.
     *
     * @param jsonStr The JSON returned by the server as a string
     * @return
     */
    fun getStatementsFromResult(jsonStr: String): Array<TinCanStatement?>? {
        var result: Array<TinCanStatement?>? = null
        try {
            val resultObj = JSONObject(jsonStr)
            if (resultObj.has("statements")) {
                //this is a StatementResult object
                val stmtArray = resultObj.getJSONArray("statements")
                result = arrayOfNulls(stmtArray.length())
                for (i in result.indices) {
                    result[i] = TinCanStatement(stmtArray.getJSONObject(i))
                }
            } else {
                //this is an individual statement
                result = arrayOf(TinCanStatement(resultObj))
            }
        } catch (e: JSONException) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 192, jsonStr, e)
        }

        return result
    }


    /**
     * Gets the registration from a JSONObject representing an XAPI statement
     * if it has one
     *
     * @param stmt JSONObject that represents a TinCan statement
     * @return The registration UUID if present in the context of statement, null otherwise
     */
    fun getStatementRegistration(stmt: JSONObject): String? {
        try {
            if (stmt.has("context")) {
                val context = stmt.getJSONObject("context")
                if (context.has("registration")) {
                    return context.getString("registration")
                }
            }
        } catch (e: JSONException) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 194, null, e)
        }

        return null
    }

}
