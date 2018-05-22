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
package com.ustadmobile.core.controller;

import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.UserSettingItem;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.UserSettingsView;

/**
 * Used for the user to change/save settings.
 * 
 * @author mike
 */
public class UserSettingsController extends UstadBaseController implements UstadController {
    
    public static final String[] SETTINGS = new String[]{"Language"};
    
    public static final String PREFKEY_LANG = "lang";
    
    public static final int SETTING_LANG = 0;

    public static final int SETTING_SUPERNODE = 1;
    
    private UserSettingsView settingsView;
    
    String[] languageNames;
    
    String[] languageCodes;
    
    public UserSettingsController(Object context) {
        super(context);
    }
    
    public static String getLocaleNameByCode(String langCode, Object context) {
        String retVal = null;
        if(langCode.equals("")) {
            //this is the system default language
            return UstadMobileSystemImpl.getInstance().getString(MessageID.lang_sys, context);
        }
        
        for(int i = 0; i < UstadMobileConstants.SUPPORTED_LOCALES.length; i++) {
            if(UstadMobileConstants.SUPPORTED_LOCALES[i][UstadMobileConstants.LOCALE_CODE].equals(langCode)) {
                return UstadMobileConstants.SUPPORTED_LOCALES[i][UstadMobileConstants.LOCALE_NAME];
            }
        }
        
        return null;
    }
    
    public static UserSettingsController makeControllerForView(UserSettingsView view) {
        UserSettingsController ctrl = new UserSettingsController(view.getContext());
        ctrl.settingsView = view;
        ctrl.setUIStrings();
        
        
        return ctrl;
    }

    
    public void setUIStrings() {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        settingsView.setSettingsTitle(impl.getString(MessageID.settings, getContext()));
        setViewSettingsList();
        
        int numLangs = UstadMobileConstants.SUPPORTED_LOCALES.length + 1;
        languageNames = new String[numLangs];
        languageCodes = new String[numLangs];
        
        //default system locale
        languageCodes[0] = "";
        languageNames[0] = impl.getString(MessageID.lang_sys, getContext());
        
        for(int i = 1; i < numLangs; i++) {
            languageCodes[i] = UstadMobileConstants.SUPPORTED_LOCALES[i-1][UstadMobileConstants.LOCALE_CODE];
            languageNames[i] = UstadMobileConstants.SUPPORTED_LOCALES[i-1][UstadMobileConstants.LOCALE_NAME];
        }
        
        settingsView.setLanguageList(languageNames);
        settingsView.setDirection(
                UstadMobileSystemImpl.getInstance().getDirection());
    }
    
    
    
    private void setViewSettingsList() {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        UserSettingItem[] items = new UserSettingItem[1];
        
        String userLangSetting = impl.getUserPref(PREFKEY_LANG, "", 
                settingsView.getContext());
        String selectedLang = getLocaleNameByCode(userLangSetting, getContext());
        items[SETTING_LANG] = new UserSettingItem(
                impl.getString(MessageID.language, getContext()), selectedLang);
        
        settingsView.setSettingsList(items);
    }

    public void handleClickAccount() {

    }

    public void handleClickDataUsage() {

    }

    
    public void handleClickSetting(int index) {
        switch(index) {
            case SETTING_LANG:
                settingsView.showLanguageList();
                break;
        }
        
    }
    
    public void handleClickLanguage(int index) {
        String chosenLocaleCode = languageCodes[index];
        UstadMobileSystemImpl.getInstance().setUserPref(PREFKEY_LANG, 
            chosenLocaleCode, context);
        setUIStrings();
        settingsView.showSettingsList();
    }

    /**
     * Handle when the user selects to logout
     */
    public void handleClickLogout() {
        LoginController.handleLogout(getContext(), BasePointView.VIEW_NAME);
    }
    
    
    
    
}
