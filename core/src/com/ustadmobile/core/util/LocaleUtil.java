/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.util;

/**
 *
 * @author mike
 */
public class LocaleUtil {
    
    public static final String subone = "%s";
    
    /**
     * If the system locale is available - choose it.  Right now - works on only
     * the basis of the first two letters (e.g. language).  We don't have variations
     * right now
     * 
     * @param systemLocale The locale according to the operating system
     * @param supportedLocales The locales available that we support
     * @param fallbackLang The language to use if the user's preferred system locale is not available (e.g. en)
     * 
     * @return locale to used based on arguments
     */
    public static String chooseSystemLocale(String systemLocale, String[] supportedLocales, String fallbackLang) {
        String systemLocaleShort = systemLocale.substring(0, 2);
        int i;
        for(i = 0; i < supportedLocales.length; i++) {
            if(supportedLocales[i].startsWith(systemLocaleShort)) {
                return supportedLocales[i];
            }
        }
        
        return fallbackLang;
    }
    
    /**
     * Formats a message which requires one substitution e.g. You chose: %s
     * 
     * @param message The main string with a %s where the variable will be substituted
     * @param substitution The string to put in place of %s
     * 
     * @return 
     */
    public static String formatMessage(String message, String substitution) {
        int i = message.indexOf(subone);
        StringBuffer sb = new StringBuffer();
        if(i != 0) {
            sb.append(message.substring(0, i));
        }
        sb.append(substitution);
        if(i < message.length()-1) {
            sb.append(message.substring(i+2));
        }
        
        return sb.toString();
    }
    
}
