package com.ustadmobile.core.view;

/**
 * Created by mike on 7/17/17.
 */

public interface WelcomeView extends UstadView {

    String VIEW_NAME = "WelcomeView";

    void setDontShowAgainChecked(boolean checked);

    void dismiss();
}
