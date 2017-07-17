package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.WelcomeView;

/**
 * Created by mike on 7/17/17.
 */

public class WelcomeController extends UstadBaseController {

    public static final String PREF_KEY_WELCOME_DONT_SHOW = "welcome.dontshow";

    public WelcomeController(Object context, WelcomeView view) {
        super(context);
        setView(view);
        view.setDontShowAgainChecked(UstadMobileSystemImpl.getInstance().getAppPref(
                PREF_KEY_WELCOME_DONT_SHOW, "true", context).equals("true"));

    }

    public void setUIStrings() {

    }

    public void handleClickHideWelcomeNextTime(boolean checked) {
        UstadMobileSystemImpl.getInstance().setAppPref(PREF_KEY_WELCOME_DONT_SHOW, String.valueOf(checked),
                getContext());
    }


}
