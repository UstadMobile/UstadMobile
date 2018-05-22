package com.ustadmobile.core.view;

/**
 * Created by kileha3 on 13/02/2017.
 */

public interface SettingsDataUsageView extends UstadView {

    public static final String VIEW_NAME="SettingsDataUsageActivity";

    /**
     * Sets the display of the superNode setting (e.g. radio button for enabled/disabled etc)
     * @param enabled
     */
    void setSupernodeEnabled(boolean enabled);

    void setSupernodeSettingVisible(boolean visible);
    void setOnlyWiFiConnection(boolean isWiFiOnly);
    void setOnlyMobileConnection(boolean isWiFiOnly);


}
