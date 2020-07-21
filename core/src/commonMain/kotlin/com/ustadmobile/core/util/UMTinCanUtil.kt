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

import com.soywiz.klock.ISO8601
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.json.json
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

    const val ADL_PREFIX_VERB = "http://adlnet.gov/expapi/verbs/"

    const val VERB_PASSED = ADL_PREFIX_VERB + "passed"

    const val VERB_FAILED = ADL_PREFIX_VERB + "failed"

    const val VERB_ANSWERED = ADL_PREFIX_VERB + "answered"


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
    fun makePageViewStmt(tinCanID: String, pageTitle: String, pageLang: String, duration: Long, actor: JsonObject): JsonObject {

        val stmtObject = json { "actor" to actor }
        val activityDef = json {
            "type" to "http://adlnet.gov/expapi/activities/module"
            "name" to makeLangMapVal(pageLang, pageTitle)
            "description" to makeLangMapVal(pageLang, pageTitle)
        }
        val objectDef = json {
            "id" to tinCanID
            "definition" to activityDef
            "objectType" to "Activity"
        }

        stmtObject.plus("object" to objectDef)

        val verbDef = json { "id" to "http://adlnet.gov/expapi/verbs/experienced" }
        val verbDisplay = json { "en-US" to "experienced" }

        verbDef.plus("display" to verbDisplay)

        stmtObject.plus("verb" to verbDef)

        val resultDef = json { "duration" to format8601Duration(duration) }

        stmtObject.plus("result" to resultDef)

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
    fun makeActivityObjectById(id: String): JsonObject {
        val definitionVal: Any? = null
        return json {
            "id" to id
            "objectType" to "Activity"
            "definition" to definitionVal
        }
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
    fun makeLangMapVal(lang: String, langValue: String): JsonObject {
        return json { lang to langValue }
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
        durationRemaining %= msPerMin

        val msPerS = 1000
        val secs = floor((durationRemaining / msPerS).toDouble()).toInt()

        return "PT" + hours + "H" + mins + "M" + secs + "S"
    }


    fun parse8601Duration(duration: String): Long {
        val time = ISO8601.INTERVAL_COMPLETE0.tryParse(duration, false)
        return time?.totalMilliseconds?.toLong() ?: 0L
    }

    fun parse8601DurationOrDefault(duration: String?, defaultDuration: Long = 0L): Long {
        return if(duration != null) {
            parse8601Duration(duration)
        }else {
            defaultDuration
        }
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
    fun makeActorFromUserAccount(username: String, xAPIServer: String): JsonObject {
        val accountObj = json {
            "homePage" to xAPIServer
            "name" to username
        }
        return json {
            "account" to accountObj
            "objectType" to "Agent"
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
    fun makeVerbObject(id: String, lang: String, display: String): JsonObject {
        return json {
            "id" to id
            "display" to makeLangMapVal(lang, display)
        }
    }

    /**
     * Gets the registration from a JSONObject representing an XAPI statement
     * if it has one
     *
     * @param stmt JSONObject that represents a TinCan statement
     * @return The registration UUID if present in the context of statement, null otherwise
     */
    fun getStatementRegistration(stmt: JsonObject): String? {
        if (stmt.containsKey("context")) {
            val context = stmt["context"] as JsonObject
            if (context.containsKey("registration")) {
                return context["registration"]?.content
            }
        }
        return null
    }

}
