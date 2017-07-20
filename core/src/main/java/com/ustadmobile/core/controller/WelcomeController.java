package com.ustadmobile.core.controller;

import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.WelcomeView;

/**
 * Created by mike on 7/17/17.
 */

public class WelcomeController extends UstadBaseController {

    public static final String PREF_KEY_WELCOME_DONT_SHOW = "welcome.dontshow";

    public static final String PREF_KEY_WELCOME_DONT_SHOW_TRANSIENT = "welcome.dontshowt";

    public WelcomeController(Object context, WelcomeView view) {
        super(context);
        setView(view);
    }

    public void setUIStrings() {

    }

    public void setHideWelcomeNextTime(boolean checked) {
        UstadMobileSystemImpl.getInstance().setAppPref(PREF_KEY_WELCOME_DONT_SHOW, String.valueOf(checked),
                getContext());
    }


}
