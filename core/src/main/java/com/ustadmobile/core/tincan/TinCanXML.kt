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
package com.ustadmobile.core.tincan

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

import java.io.IOException
import java.util.Hashtable

/**
 * This class represents a tincan.xml file of a given container. As specified here:
 * https://github.com/RusticiSoftware/launch/blob/master/lms_lrs.md
 *
 * @author mike
 */
class TinCanXML {

    /**
     * The ID of the activity which has a launch element (if any)
     */
    /**
     * Returns the Activity for which contained a launch element.  As per the spec
     * only one activity in a given tincan.xml file may contain a launch
     * element.
     *
     * @return the Activity for which there is a launch tag (if any) was found
     */
    var launchActivity: Activity? = null
        private set

    private val isRegistrationResumable: Boolean = false

    private val activities: Hashtable<*, *>? = null

    companion object {

        val PARSE_POPULATE_ACTIVITIES = 1

        @Throws(XmlPullParserException::class, IOException::class)
        @JvmOverloads
        fun loadFromXML(xpp: XmlPullParser, parseFlags: Int = 0): TinCanXML {
            val tcxml = TinCanXML()
            var activity: Activity? = null
            var evtType = xpp.eventType

            val storeActivities = parseFlags and PARSE_POPULATE_ACTIVITIES == PARSE_POPULATE_ACTIVITIES
            var inExtensions = false
            var tagName: String
            var extKey: String
            var extVal: String

            do {
                if (evtType == XmlPullParser.START_TAG && xpp.name != null) {
                    tagName = xpp.name

                    if (!inExtensions) {
                        if (tagName == "activity") {
                            activity = Activity(xpp.getAttributeValue(null, "id"),
                                    xpp.getAttributeValue(null, "type"))
                        } else if (tagName == "launch" && xpp.next() == XmlPullParser.TEXT) {
                            activity!!.launchUrl = xpp.text
                        } else if (tagName == "name" && xpp.next() == XmlPullParser.TEXT) {
                            activity!!.name = xpp.text
                        } else if (tagName == "description" && xpp.next() == XmlPullParser.TEXT) {
                            activity!!.desc = xpp.text
                        } else if (xpp.name == "extensions") {
                            inExtensions = true
                        }
                    } else {
                        if (tagName == "extension") {
                            extKey = xpp.getAttributeValue(null, "key")
                            if (xpp.next() == XmlPullParser.TEXT) {
                                extVal = xpp.text
                            } else {
                                extVal = ""
                            }
                            activity!!.setExtension(extKey, extVal)
                        }
                    }
                } else if (evtType == XmlPullParser.END_TAG) {
                    if (xpp.name != null) {
                        if (xpp.name == "activity") {
                            if (activity!!.launchUrl != null) {
                                tcxml.launchActivity = activity

                                //if we aer just looking to get the launch activity - we're finished
                                if (!storeActivities) {
                                    break
                                }
                            }
                        } else if (xpp.name == "extensions") {
                            inExtensions = false
                        }
                    }
                }

                evtType = xpp.next()
            } while (evtType != XmlPullParser.END_DOCUMENT)

            return tcxml
        }
    }

}
