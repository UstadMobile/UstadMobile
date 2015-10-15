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

import com.ustadmobile.core.U;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.model.UserSettingItem;
import com.ustadmobile.core.view.UserSettingsView;
import java.util.Hashtable;

/**
 * Used for the user to change/save settings.
 * 
 * @author mike
 */
public class UserSettingsController extends UstadBaseController implements UstadController {
    
    public static final String[] SETTINGS = new String[]{"Language"};
    
    public static final String PREFKEY_LANG = "lang";
    
    public static final int SETTING_LANG = 0;
    
    private UserSettingsView settingsView;
    
    private static Hashtable langNamesTable = new Hashtable();
    
    static {
        langNamesTable.put("", new Integer(U.id.lang_sys));
        langNamesTable.put("en", new Integer(U.id.lang_en));
        langNamesTable.put("ar", new Integer(U.id.lang_ar));
    }
    
    String[] languageNames;
    
    String[] languageCodes;
    
    public UserSettingsController(Object context) {
        super(context);
    }
    
    public static int getStringIDByLang(String langCode) {
        if(langNamesTable.containsKey(langCode)) {
            return ((Integer)langNamesTable.get(langCode)).intValue();
        }else {
            return -1;
        }
    }
    
    public static UserSettingsController makeControllerForView(UserSettingsView view) {
        UserSettingsController ctrl = new UserSettingsController(view.getContext());
        ctrl.settingsView = view;
        ctrl.setUIStrings();
        
        
        return ctrl;
    }

    
    public void setUIStrings() {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        settingsView.setSettingsTitle(impl.getString(U.id.settings));
        setViewSettingsList();
        
        int numLangs = UstadMobileConstants.supportedLocales.length + 1;
        languageNames = new String[numLangs];
        languageCodes = new String[numLangs];
        
        //default system locale
        languageCodes[0] = "";
        languageNames[0] = impl.getString(U.id.lang_sys);
        
        String locale;
        for(int i = 1; i < numLangs; i++) {
            locale = UstadMobileConstants.supportedLocales[i-1];
            languageCodes[i] = locale;
            languageNames[i] = impl.getString(getStringIDByLang(locale));
        }
        
        settingsView.setLanguageList(languageNames);
    }
    
    
    
    private void setViewSettingsList() {
        final UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        UserSettingItem[] items = new UserSettingItem[1];
        
        String userLangSetting = impl.getUserPref(PREFKEY_LANG, "", 
                settingsView.getContext());
        String selectedLang = impl.getString(getStringIDByLang(userLangSetting));
        items[SETTING_LANG] = new UserSettingItem("Language", selectedLang);
        
        settingsView.setSettingsList(items);
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
            chosenLocaleCode, index);
        UstadMobileSystemImpl.getInstance().loadLocale(getContext());
        setUIStrings();
        settingsView.showSettingsList();
    }
    
    
    
    
}
