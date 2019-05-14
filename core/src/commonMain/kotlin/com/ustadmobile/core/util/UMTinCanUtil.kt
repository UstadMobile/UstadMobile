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

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.Vector
import kotlin.math.floor

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
            UMLog.l(UMLog.ERROR, 197, "$lang/$langValue",
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
        val hours = floor((duration / msPerHour).toDouble()).toInt()
        var durationRemaining = duration % msPerHour

        val msPerMin = 60 * 1000
        val mins = floor((durationRemaining / msPerMin).toDouble()).toInt()
        durationRemaining = durationRemaining % msPerMin

        val msPerS = 1000
        val secs = floor((durationRemaining / msPerS).toDouble()).toInt()

        return "PT" + hours + "H" + mins + "M" + secs + "S"
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
            UMLog.l(UMLog.ERROR, 195, null, e)
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
        val accountUsername = account?.username
        val accountEndpoint = account?.endpointUrl
        return if (accountUsername != null && accountEndpoint != null) {
            makeActorFromUserAccount(accountUsername,
                    accountEndpoint)
        } else {
            makeActorFromUserAccount("anonymous",
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
            UMLog.l(UMLog.ERROR, 195, "UMTinCanUtil.makeVerbObject", je)
        }

        return obj
    }




}
