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
     * Index of the locale code in the string array - e.g.
     * SUPPORTED_LOCALES[i][LOCALE_CODE] = "en"
     */
    const val LOCALE_CODE = 0

    /**
     * Map of all supported UI language
     */

    val LANGUAGE_NAMES = mapOf(
            "en" to "English",
            "ps" to "\u067e\u069a\u062a\u0648",
            "fa" to "\u062f\u0631\u06cc",
            "ar" to "العربية")

}
