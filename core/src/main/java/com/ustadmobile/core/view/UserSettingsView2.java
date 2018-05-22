package com.ustadmobile.core.view;

/**
 * Created by mike on 5/30/17.
 */

public interface UserSettingsView2 extends  UstadView{

    String VIEW_NAME = "UserSettings2";

    void setActiveLanguage(String language);

    void setUserDisplayName(String name);

    void refreshLanguage();

    void setLastSyncText(String lastSyncText);

}
