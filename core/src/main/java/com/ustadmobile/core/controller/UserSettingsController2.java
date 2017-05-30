package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.BasePointView;
import com.ustadmobile.core.view.DataSettingsView;
import com.ustadmobile.core.view.SettingsDataUsageView;

/**
 * Created by mike on 5/30/17.
 */

public class UserSettingsController2 extends  UstadBaseController {

    public UserSettingsController2(Object context, boolean statusEventListeningEnabled) {
        super(context, statusEventListeningEnabled);
    }

    public UserSettingsController2(Object context) {
        super(context);
    }

    public void handleClickAccount() {

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
}
