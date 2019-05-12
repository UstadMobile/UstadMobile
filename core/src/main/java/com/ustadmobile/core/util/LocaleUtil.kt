/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.util

import com.ustadmobile.core.impl.UstadMobileConstants

/**
 *
 * @author mike
 */
object LocaleUtil {

    val SUBONE = "%s"

    /**
     * If the system locale is available - choose it.  Right now - works on only
     * the basis of the first two letters (e.g. language).  We don't have variations
     * right now
     *
     * @param userPrefLocale The locale specified by this user. Blank string "" means use the system locale, null for no current user
     * @param systemLocale The locale according to the operating system
     * @param supportedLocales The locales available that we support : two
     * dimensional string in the form of {code, name}... e.g. {"en", "English"}, {"fr", "Francais"}
     * @param fallbackLang The language to use if the user's preferred system locale is not available (e.g. en)
     *
     * @return locale to used based on arguments
     */
    fun chooseSystemLocale(userPrefLocale: String?, systemLocale: String, supportedLocales: Array<Array<String>>, fallbackLang: String): String {
        //first check the user's chosen locale is valid and overrides the systemLocale
        var i: Int
        if (userPrefLocale != null && userPrefLocale != "") {
            i = 0
            while (i < supportedLocales.size) {
                if (supportedLocales[i][UstadMobileConstants.LOCALE_CODE] == userPrefLocale) {
                    return userPrefLocale
                }
                i++
            }
        }


        val systemLocaleShort = systemLocale.substring(0, 2)

        i = 0
        while (i < supportedLocales.size) {
            if (supportedLocales[i][UstadMobileConstants.LOCALE_CODE].startsWith(systemLocaleShort)) {
                return supportedLocales[i][UstadMobileConstants.LOCALE_CODE]
            }
            i++
        }

        return fallbackLang
    }

    /**
     * Formats a message which requires one substitution e.g. You chose: %s
     *
     * @param message The main string with a %s where the variable will be substituted
     * @param substitution The string to put in place of %s
     *
     * @return
     */
    fun formatMessage(message: String, substitution: String): String {
        val i = message.indexOf(SUBONE)

        if (i == -1) {//incorrectly done translation: missing %s
            return message
        }

        val sb = StringBuffer()
        if (i != 0) {
            sb.append(message.substring(0, i))
        }
        sb.append(substitution)
        if (i < message.length - 1) {
            sb.append(message.substring(i + 2))
        }

        return sb.toString()
    }

}
