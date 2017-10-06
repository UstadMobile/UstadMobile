package com.ustadmobile.core.controller;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.generated.locale.MessageID;
import com.ustadmobile.core.impl.UstadMobileConstants;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.util.UMUtil;
import com.ustadmobile.core.view.AppViewChoiceListener;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.RegistrationView;
import com.ustadmobile.core.view.SettingsDataUsageView;
import com.ustadmobile.core.view.UserSettingsView2;

import java.util.Hashtable;

/**
 * Created by mike on 5/30/17.
 */

public class UserSettingsController2 extends  UstadBaseController implements AppViewChoiceListener{

    private static final int CMD_SET_LANG = 1;

    private UserSettingsView2 view;

    private String localeOnCreate = null;

    public UserSettingsController2(Object context, Hashtable args, UserSettingsView2 view) {
        super(context);
        setView(view);
        this.view = view;
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        localeOnCreate = impl.getDisplayedLocale(context);
        if(impl.getActiveUser(context) != null)
            view.setUserDisplayName(impl.getActiveUser(context));

    }

    public void handleClickAccount() {
        //UstadMobileSystemImpl.getInstance().go(AccountSettingsView.VIEW_NAME, getContext());
        UstadMobileSystemImpl.getInstance().go(RegistrationView.VIEW_NAME, getContext());
    }

    public void handleClickLanguage() {
        UstadMobileSystemImpl impl = UstadMobileSystemImpl.getInstance();
        String[] availableLocales = new String[CoreBuildConfig.SUPPORTED_LOCALES.length + 1];
        availableLocales[0] = impl.getString(MessageID.device_language, getContext());
        for(int i = 0; i < CoreBuildConfig.SUPPORTED_LOCALES.length; i++) {
            availableLocales[i + 1] = UstadMobileConstants.LANGUAGE_NAMES.get(
                    CoreBuildConfig.SUPPORTED_LOCALES[i]).toString();
        }

        UstadMobileSystemImpl.getInstance().getAppView(getContext()).showChoiceDialog(
                impl.getString(MessageID.language, getContext()),
                availableLocales, CMD_SET_LANG, this);
    }

    public void handleClickDataSettings() {
        UstadMobileSystemImpl.getInstance().go(SettingsDataUsageView.VIEW_NAME, getContext());
    }

    public void handleClickLogout() {
        LoginController.handleLogout(getContext(), BasePointView.VIEW_NAME);
    }

    public void setUIStrings() {

    }

    public void appViewChoiceSelected(int commandId, int choice) {
        String chosenLocale = choice == 0
            ? UstadMobileSystemImpl.LOCALE_USE_SYSTEM
            : CoreBuildConfig.SUPPORTED_LOCALES[choice - 1];

        UstadMobileSystemImpl.getInstance().setLocale(chosenLocale, getContext());
        UstadMobileSystemImpl.getInstance().getAppView(getContext()).dismissChoiceDialog();

        if(UMUtil.hasDisplayedLocaleChanged(localeOnCreate, getContext())) {
            view.refreshLanguage();
        }
    }
}
