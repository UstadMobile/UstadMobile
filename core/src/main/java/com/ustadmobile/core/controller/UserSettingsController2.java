package com.ustadmobile.core.controller;

import com.ustadmobile.core.buildconfig.CoreBuildConfig;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.AppViewChoiceListener;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.DataSettingsView;
import com.ustadmobile.core.view.SettingsDataUsageView;

/**
 * Created by mike on 5/30/17.
 */

public class UserSettingsController2 extends  UstadBaseController implements AppViewChoiceListener{

    private static final int CMD_SET_LANG = 1;

    public UserSettingsController2(Object context, boolean statusEventListeningEnabled) {
        super(context, statusEventListeningEnabled);
    }

    public UserSettingsController2(Object context) {
        super(context);
    }

    public void handleClickAccount() {

    }

    public void handleClickLanguage() {
        UstadMobileSystemImpl.getInstance().getAppView(getContext()).showChoiceDialog("Language",
                CoreBuildConfig.SUPPORTED_LOCALES, CMD_SET_LANG, this);
    }

    public void handleClickDataSettings() {
        UstadMobileSystemImpl.getInstance().go(SettingsDataUsageView.VIEW_NAME, getContext());
    }

    public void handleClickLogout() {
        LoginController.handleLogout(getContext(), BasePointView.VIEW_NAME);
    }

    @Override
    public void setUIStrings() {

    }

    @Override
    public void appViewChoiceSelected(int commandId, int choice) {
        String chosenLocale = CoreBuildConfig.SUPPORTED_LOCALES[choice];
        UstadMobileSystemImpl.getInstance().setLocale(chosenLocale, getContext());
        UstadMobileSystemImpl.getInstance().getAppView(getContext()).dismissChoiceDialog();
    }
}
