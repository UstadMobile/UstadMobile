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
package com.ustadmobile.core.impl


/**
 *
 * @author mike
 */
object UstadMobileConstants {

    /**
     * Hardcode UTF8 Encoding String - value "UTF-8"
     */
    const val UTF8 = "UTF-8"

    /**
     * Hardcoded subdirectory used for caching
     */
    const val CACHEDIR = "cache"

    /**
     * Locales and their name (in their own language).  Two dimensional string
     * array with the locale code and it's name
     */
    val SUPPORTED_LOCALES = arrayOf(arrayOf("en", "English"), arrayOf("ar", "\u0627\u0644\u0639\u064e\u0631\u064e\u0628\u0650\u064a\u0629\u200e"), arrayOf("fa", "\u062f\u0631\u06cc"), arrayOf("ps", "\u067e\u069a\u062a\u0648"))

    /**
     * Index of the locale code in the string array - e.g.
     * SUPPORTED_LOCALES[i][LOCALE_CODE] = "en"
     */
    const val LOCALE_CODE = 0


    val fallbackLocale = "en"

    val LANGUAGE_NAMES: MutableMap<String, String> = HashMap()

    init {
        LANGUAGE_NAMES["en"] = "English"
        LANGUAGE_NAMES["en-US"] = "English (US)"
        LANGUAGE_NAMES["ps"] = "\u067e\u069a\u062a\u0648"
        LANGUAGE_NAMES["fa-AF"] = "\u062f\u0631\u06cc"
        LANGUAGE_NAMES["fa"] = "\u062f\u0631\u06cc"
    }

}
